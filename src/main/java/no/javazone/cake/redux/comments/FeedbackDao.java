package no.javazone.cake.redux.comments;

import java.util.List;

public interface FeedbackDao {
    void addFeedback(Feedback feedback);
    void deleteFeedback(String id);
    List<Feedback> feedbacksForTalk(String talkid);

    static FeedbackDao instance() {
        return FeedbackDaoFileImpl.get();
    }
}
