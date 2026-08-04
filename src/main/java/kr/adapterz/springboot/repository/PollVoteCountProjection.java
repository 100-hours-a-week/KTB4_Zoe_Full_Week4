package kr.adapterz.springboot.repository;

public interface PollVoteCountProjection {

    Long getOptionId();

    long getVoteCount();
}
