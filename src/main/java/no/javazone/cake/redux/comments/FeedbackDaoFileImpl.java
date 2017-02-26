package no.javazone.cake.redux.comments;

import no.javazone.cake.redux.CommunicatorHelper;
import no.javazone.cake.redux.Configuration;
import org.jsonbuddy.parse.JsonParser;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class FeedbackDaoFileImpl implements FeedbackDao {
    private static transient FeedbackDaoFileImpl _instance;

    public static synchronized FeedbackDaoFileImpl get() {
        if (_instance == null) {
            _instance = new FeedbackDaoFileImpl();
        }
        return _instance;
    }
    private final ExecutorService executorService;
    private final transient Set<Feedback> feedbacks;

    private FeedbackDaoFileImpl() {
        executorService = Executors.newSingleThreadExecutor();
        this.feedbacks = new HashSet<>();

        String filename = Configuration.feedbackStoreFilename();
        if (filename == null) {
            return ;
        }
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Creating storage file '" + filename + "'");
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        String stored;
        try {
            stored = CommunicatorHelper.toString(new FileInputStream(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Arrays.asList(stored.split("\n")).stream()
                .filter(line -> !line.trim().isEmpty())
                .map(JsonParser::parseToObject)
                .map(Feedback::fromStoreJson)
                .forEach(feedbacks::add);


    }


    @Override
    public void addFeedback(Feedback feedback,String talkLastModified) {
        synchronized (feedbacks) {
            feedbacks.add(feedback);
        }
        String filename = Configuration.feedbackStoreFilename();
        if (filename == null) {
            return;
        }
        executorService.submit(() -> {
            try(FileWriter fw = new FileWriter(filename, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter writer = new PrintWriter(bw))
            {
                feedback.asStoreJson().toJson(writer);
                writer.append("\n");
            } catch (IOException e) {
            }
        });
    }

    @Override
    public String deleteFeedback(String talkRef, String id,String lastModified) {
        HashSet<Feedback> duplicate;
        synchronized (feedbacks) {
            Optional<Feedback> feedbackOptional = feedbacks.stream()
                    .filter(fe -> fe.id.equals(id))
                    .findAny();
            if (feedbackOptional.isPresent()) {
                feedbacks.remove(feedbackOptional.get());
            }
            duplicate = new HashSet<>(this.feedbacks);
        }
        String filename = Configuration.feedbackStoreFilename();
        if (filename == null) {
            return "xx";
        }
        executorService.submit(() -> {
            try(FileWriter fw = new FileWriter(filename, false);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter writer = new PrintWriter(bw))
            {
                duplicate.forEach(feedback -> {
                    feedback.asStoreJson().toJson(writer);
                    writer.append("\n");
                });
            } catch (IOException e) {
            }

        });
        return "xx";
    }

    @Override
    public Stream<Feedback> feedbacksForTalk(String talkid) {
        synchronized (feedbacks) {
            return feedbacks.stream()
                    .filter(fe -> fe.talkid.equals(talkid));

        }
    }

}
