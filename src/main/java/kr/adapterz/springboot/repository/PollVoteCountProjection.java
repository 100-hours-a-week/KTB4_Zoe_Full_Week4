package kr.adapterz.springboot.repository;

public interface PollVoteCountProjection {

    Long getPollId();

    Long getOptionId();

    long getVoteCount();
}
