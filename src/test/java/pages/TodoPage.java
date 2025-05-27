package pages;

import com.microsoft.playwright.Page;

import com.microsoft.playwright.options.WaitForSelectorState;
import utils.ConfigReader;

public class TodoPage {
    private final Page page;


    public TodoPage(Page page) {
        this.page = page;
    }

    public void navigate() {
        page.navigate(ConfigReader.getProperty("app.url"));
        page.waitForSelector(".todoapp", new Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.ATTACHED)
                .setTimeout(10000));
    }

    public void addTodo(String todoText) {
        int initialCount = getTodosCount();
        page.locator(ConfigReader.getProperty("new.todo.selector")).fill(todoText);
        page.locator(ConfigReader.getProperty("new.todo.selector")).press("Enter");


        if (!todoText.trim().isEmpty()) {
            page.waitForFunction(
                    "([expected, text]) => {"
                            + "const items = Array.from(document.querySelectorAll('" + ConfigReader.getProperty("todo.list.selector") + "'));"
                            + "return items.length === expected && items.some(i => i.querySelector('label').textContent === text);"
                            + "}",
                    new Object[]{initialCount + 1, todoText},
                    new Page.WaitForFunctionOptions().setTimeout(10000)
            );
        }
    }

    public int getTodosCount() {
        return page.locator(ConfigReader.getProperty("todo.list.selector")).count(); // Возвращаем количество задач
    }


    public void clearCompleted() {
        page.waitForSelector(ConfigReader.getProperty("clear.completed.selector") + ":visible", new Page.WaitForSelectorOptions()
                .setTimeout(10000));
        page.locator(ConfigReader.getProperty("clear.completed.selector")).click();
        page.waitForTimeout(500);
    }


    public void completeTodo(int i) {
        // Находим элемент чекбокса для выполнения задачи по индексу
        page.locator(ConfigReader.getProperty("todo.list.selector") + " >> nth=" + i).locator("input.toggle").click();
    }

    public void filterCompleted() {
        page.locator(ConfigReader.getProperty("completed")).click();
    }

    public double getVisibleTodosCount() {
        // Получаем селектор для #todo-title из config.properties
        String todoTitleSelector = ConfigReader.getProperty("todo.title.selector");

        // Проверяем, что элемент с id "todo-title" видим и существует
        if (page.locator(todoTitleSelector).count() == 1 && page.locator(todoTitleSelector).isVisible()) {
            // Возвращаем количество видимых задач в списке
            return page.locator(ConfigReader.getProperty("todo.list.selector") + ":visible").count();
        } else {
            // Если #todo-title не найден или не видим, возвращаем 0
            return 0;
        }
    }

    public void filterActive() {
        page.locator(ConfigReader.getProperty("active")).click();
    }

    public void reloadAndKeepState() {
        page.reload();
    }
}
