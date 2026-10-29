package com.example.lasttime.data.repository

// Interfaces DAO do Room para acesso às tabelas
import com.example.lasttime.data.local.dao.ActivityDao
import com.example.lasttime.data.local.dao.CategoryDao

// Entidades de banco de dados
import com.example.lasttime.data.local.entity.ActivityEntity
import com.example.lasttime.data.local.entity.CategoryEntity

// Modelos de domínio e regras de negócio
import com.example.lasttime.domain.model.Activity
import com.example.lasttime.domain.model.ActivityNotFoundException
import com.example.lasttime.domain.model.ActivityRules
import com.example.lasttime.domain.model.InvalidActivityException
import com.example.lasttime.domain.model.MarkOutcome
import com.example.lasttime.domain.model.calculateDaysSince

// Corrotinas e reatividade com Kotlin Flow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Repositório central para gerenciamento do ciclo de vida das Atividades, Histórico e Categorias.
 *
 * Atua como intermediário entre a camada de dados (Room DAOs) e a camada de apresentação (ViewModels).
 * Aplica regras de validação de negócios e conversão entre Entidades do Room e Modelos de Domínio.
 *
 * @property dao Interface DAO para operações na tabela de atividades e histórico.
 * @property categoryDao Interface DAO para operações na tabela de categorias.
 */
class ActivityRepository(
    private val dao: ActivityDao,
    private val categoryDao: CategoryDao
) {

    /**
     * Observa a lista contínua de atividades ordenadas e convertidas para o domínio.
     *
     * @return [Flow] que emite a lista atualizada de [Activity] sempre que o banco for alterado.
     */
    fun observeActivities(): Flow<List<Activity>> =
        dao.observeActivities().map { list ->
            val today = LocalDate.now()
            // Mapeia cada entidade de banco para o modelo de domínio calculando os dias decorridos
            list.map { it.toDomain(today) }
        }

    /**
     * Observa os nomes de todas as categorias cadastradas no banco de dados em tempo real.
     *
     * @return [Flow] emitindo uma lista de Strings com os nomes das categorias.
     */
    fun observeCategories(): Flow<List<String>> =
        categoryDao.observeCategories().map { list -> list.map { it.name } }

    /**
     * Observa o histórico de datas de realização de uma atividade específica.
     *
     * @param activityId ID da atividade consultada.
     * @return [Flow] emitindo a lista de datas em que a atividade foi realizada.
     */
    fun observeHistory(activityId: Long): Flow<List<LocalDate>> =
        dao.observeHistory(activityId).map { list -> list.map { it.performedAt } }

    /**
     * Cria e insere uma nova categoria no banco de dados local.
     *
     * @param name Nome da nova categoria.
     * @return [Result] de sucesso ou falha encapsulado com exceções de validação.
     */
    suspend fun addCategory(name: String): Result<Unit> = safeCall {
        val trimmed = name.trim()
        // Validação de string vazia
        if (trimmed.isBlank()) {
            throw InvalidActivityException("O nome da categoria não pode ser vazio.")
        }
        // Validação de unicidade (case-insensitive)
        if (categoryDao.countCategoryByName(trimmed) > 0) {
            throw InvalidActivityException("Essa categoria já existe.")
        }
        // Insere a entidade na tabela de categorias
        categoryDao.insertCategory(CategoryEntity(name = trimmed))
    }

    /**
     * Adiciona uma nova atividade no banco e grava o seu primeiro registro no histórico.
     *
     * @param name Nome da atividade.
     * @param performedAt Data da realização inicial.
     * @param category Categoria associada (Padrão: "Trabalho").
     * @param notes Anotações adicionais.
     * @return [Result] com o ID gerado para a nova atividade.
     */
    suspend fun addActivity(
        name: String,
        performedAt: LocalDate,
        category: String = "Trabalho",
        notes: String = ""
    ): Result<Long> = safeCall {
        // Validação das regras de nome (não vazio, tamanho máximo, etc.)
        ActivityRules.validateName(name)?.let { throw InvalidActivityException(it) }
        // Impede que a data informada seja no futuro
        if (performedAt.isAfter(LocalDate.now())) {
            throw InvalidActivityException("A data não pode estar no futuro.")
        }
        // Operação atômica em transação no Room
        dao.insertActivityWithHistory(name.trim(), performedAt, category, notes)
    }

    /**
     * Registra a realização de uma atividade na data de hoje.
     *
     * @param activityId ID da atividade a ser atualizada.
     * @return [Result] contendo o resultado da operação [MarkOutcome] (MARKED, ALREADY_MARKED_TODAY, NOT_FOUND).
     */
    suspend fun markPerformedToday(activityId: Long): Result<MarkOutcome> = safeCall {
        val outcome = dao.markPerformed(activityId, LocalDate.now())
        if (outcome == MarkOutcome.NOT_FOUND) throw ActivityNotFoundException()
        outcome
    }

    /**
     * Remove uma atividade do banco de dados pelo seu ID.
     *
     * @param activityId ID da atividade a ser excluída.
     * @return [Result] de confirmação da remoção.
     */
    suspend fun deleteActivity(activityId: Long): Result<Unit> = safeCall {
        if (dao.deleteActivity(activityId) == 0) throw ActivityNotFoundException()
    }

    /**
     * Popula o banco de dados com dados pré-cadastrados (categorias e atividades padrão)
     * caso seja a primeira inicialização da aplicação.
     */
    suspend fun seedInitialDataIfEmpty() {
        // Popula as categorias iniciais se a tabela estiver vazia
        if (categoryDao.countCategories() == 0) {
            categoryDao.insertCategory(CategoryEntity(name = "Trabalho"))
            categoryDao.insertCategory(CategoryEntity(name = "Pessoal"))
            categoryDao.insertCategory(CategoryEntity(name = "Plantas"))
        }

        // Popula as atividades de exemplo se a tabela de atividades estiver vazia
        if (dao.countActivities() == 0) {
            val today = LocalDate.now()
            // Categoria: Trabalho
            dao.insertActivityWithHistory("Atualizar a planilha semanal", today.minusDays(6), "Trabalho", "")
            dao.insertActivityWithHistory("Fazer o pagamento mensal", today.minusDays(15), "Trabalho", "")
            // Categoria: Pessoal
            dao.insertActivityWithHistory("Cortar o cabelo", today.minusDays(60), "Pessoal", "")
            dao.insertActivityWithHistory("Lavar o carro", today.minusDays(60), "Pessoal", "")
            // Categoria: Plantas
            dao.insertActivityWithHistory("Regar a suculenta", today.minusDays(7), "Plantas", "")
            dao.insertActivityWithHistory("Podar a roseira", today.minusDays(90), "Plantas", "")
        }
    }

    /**
     * Extensão interna para conversão de [ActivityEntity] (banco) para [Activity] (domínio).
     *
     * @param today Data de hoje utilizada para o cálculo da quantidade de dias decorridos.
     */
    private fun ActivityEntity.toDomain(today: LocalDate) = Activity(
        id = id,
        name = name,
        lastPerformedAt = lastPerformedAt,
        daysSinceLastPerformed = calculateDaysSince(lastPerformedAt, today),
        category = category,
        notes = notes
    )
}
