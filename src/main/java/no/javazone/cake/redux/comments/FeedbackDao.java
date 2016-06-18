package no.javazone.cake.redux.comments;

import java.util.List;
import java.util.stream.Stream;

public interface FeedbackDao {
    void addFeedback(Feedback feedback);
    void deleteFeedback(String id);
    Stream<Feedback> feedbacksForTalk(String talkid);
    List<Feedback> allFeedbacks();

    static FeedbackDao instance() {
        return FeedbackDaoFileImpl.get();
    }
}
