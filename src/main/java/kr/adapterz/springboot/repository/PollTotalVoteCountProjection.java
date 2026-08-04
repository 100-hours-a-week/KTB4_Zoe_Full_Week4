package kr.adapterz.springboot.repository;

public interface PollTotalVoteCountProjection {

    Long getPollId();

    long getTotalVoteCount();
}
