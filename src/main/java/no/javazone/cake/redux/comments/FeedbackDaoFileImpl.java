package no.javazone.cake.redux.comments;

import java.util.*;
import java.util.stream.Collectors;

public class FeedbackDaoFileImpl implements FeedbackDao {
    private static transient FeedbackDaoFileImpl _instance;

    public static synchronized FeedbackDaoFileImpl get() {
        if (_instance == null) {
            _instance = new FeedbackDaoFileImpl();
        }
        return _instance;
    }

    private FeedbackDaoFileImpl() {}

    private final transient Set<Feedback> feedbacks = new HashSet<>();


    @Override
    public void addFeedback(Feedback feedback) {
        synchronized (feedbacks) {
            feedbacks.add(feedback);
        }
    }

    @Override
    public void deleteFeedback(String id) {
        synchronized (feedbacks) {
            Optional<Feedback> feedbackOptional = feedbacks.stream()
                    .filter(fe -> fe.id.equals(id))
                    .findAny();
            if (feedbackOptional.isPresent()) {
                feedbacks.remove(feedbackOptional.get());
            }
        }
    }

    @Override
    public List<Feedback> feedbacksForTalk(String talkid) {
        synchronized (feedbacks) {
            return feedbacks.stream()
                    .filter(fe -> fe.talkid.equals(talkid))
                    .sequential()
                    .sorted((o1, o2) -> o1.created.compareTo(o2.created))
                    .collect(Collectors.toList());
        }
    }

}
