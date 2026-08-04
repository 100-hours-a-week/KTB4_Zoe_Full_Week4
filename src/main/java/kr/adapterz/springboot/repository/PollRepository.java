package kr.adapterz.springboot.repository;

import kr.adapterz.springboot.entity.Poll;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollRepository extends JpaRepository<Poll, Long> {
}
