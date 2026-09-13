package sg.edu.ntu.taskflowapi.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import sg.edu.ntu.taskflowapi.model.Task;
import sg.edu.ntu.taskflowapi.repository.TaskRepository;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ChatClient chatClient;

    public TaskService(
            TaskRepository taskRepository,
            ChatClient.Builder chatClientBuilder) {
        this.taskRepository = taskRepository;
        this.chatClient = chatClientBuilder.build();
    }

    public List<Task> findAllTasks() {
        return taskRepository.findAll();
    }

    public Task findTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public Task createTask(Task task) {
        task.setId(null);
        return taskRepository.save(task);
    }

    public Task updateTask(Long id, Task task) {
        Task existingTask = taskRepository.findById(id);
        if (existingTask == null) {
            return null;
        }

        task.setId(id);
        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public Task markTaskAsComplete(Long id) {
        Task task = taskRepository.findById(id);
        if (task == null) {
            return null;
        }

        task.setCompleted(true);
        return taskRepository.save(task);
    }

    public String generateTaskSummary() {
        List<Task> tasks = findAllTasks();

        String taskDetails = tasks.stream()
                .map(task -> String.format(
                        "Task: %s, completed: %s",
                        task.getTitle(),
                        task.isCompleted()))
                .collect(Collectors.joining("\n"));

        String prompt = """
                You are a helpful task management assistant.
                Summarise the following tasks in short plain English.
                Clearly state what is pending and what is completed.

                Tasks:
                %s
                """.formatted(taskDetails);

        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();
    }
}
