package tests;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;
import pages.TodoPage;
import utils.ConfigReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SPAProjectTest {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;
    TodoPage todoPage;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
    }

    @BeforeEach
    void createContextAndPage() {
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false)); // Запуск браузера в обычном режиме.
        context = browser.newContext(); // Создание нового контекста браузера.
        page = context.newPage(); // Открытие новой страницы в контексте.
        todoPage = new TodoPage(page); // Инициализация TodoPage с текущей страницей.
        todoPage.navigate(); // Переход на страницу приложения TodoMVC.
    }

    @Test
    @DisplayName("Проверка добавления и завершения задач")
    void testAddAndCompleteTodo() throws InterruptedException {
        todoPage.addTodo("Task 1");
        assertEquals(1, todoPage.getTodosCount(), "Количество задач после добавления первой задачи");

        todoPage.addTodo("Task 2");
        assertEquals(2, todoPage.getTodosCount(), "Количество задач после добавления второй задачи");
        todoPage.completeTodo(1);
        todoPage.filterCompleted();
        assertEquals(1, todoPage.getVisibleTodosCount(), "Количество выполненных задач должно быть 1");
        todoPage.filterActive();
        assertEquals(1, todoPage.getVisibleTodosCount(), "Количество активных задач должно быть 1");
    }

    @Test
    @DisplayName("Проверка очистки завершенных задач")
    void testClearCompleted() {
        todoPage.addTodo("Task to complete");
        todoPage.addTodo("Task to keep");

        todoPage.completeTodo(0);
        todoPage.clearCompleted();

        assertEquals(1, todoPage.getTodosCount());
        assertEquals("Task to keep",
                page.locator(ConfigReader.getProperty("todo.list.selector")).first().locator("label").textContent().trim());
    }

    @Test
    @DisplayName("Проверка сохранения состояния после перезагрузки")
    void testPersistenceAfterReload() {
        todoPage.addTodo("Persistent Task");
        todoPage.completeTodo(0);

        todoPage.reloadAndKeepState();

        assertEquals(1, todoPage.getTodosCount());
        assertTrue(page.locator(ConfigReader.getProperty("todo.list.selector")).first()
                .getByRole(AriaRole.CHECKBOX).isChecked());
    }

    @Test
    @DisplayName("Проверка добавления пустой задачи")
    void testAddEmptyTodo() {
        int initialCount = todoPage.getTodosCount();
        todoPage.addTodo("   ");
        assertEquals(initialCount, todoPage.getTodosCount(), "Пустая задача не должна добавляться");
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) {
            browser.close(); // Закрываем браузер, если он открыт.
        }
        playwright.close(); // Закрываем Playwright.
    }

    @AfterEach
    void closeContext() {
        if (context != null) {
            context.close(); // Закрываем контекст, если он существует.
        }
    }
}
