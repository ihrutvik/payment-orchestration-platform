package com.hrutvik.payments.application;

import com.hrutvik.payments.domain.ProviderResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProviderCircuitBreaker {
  private final int failureThreshold;
  private final Duration openDuration;
  private final Clock clock;
  private final ConcurrentHashMap<String,State> states=new ConcurrentHashMap<>();

  @Autowired
  public ProviderCircuitBreaker(
      @Value("${payments.providers.circuit-breaker.failure-threshold:3}") int failureThreshold,
      @Value("${payments.providers.circuit-breaker.open-duration:30s}") Duration openDuration){
    this(failureThreshold,openDuration,Clock.systemUTC());
  }

  public ProviderCircuitBreaker(int failureThreshold,Duration openDuration,Clock clock){
    if(failureThreshold<1) throw new IllegalArgumentException("failureThreshold must be positive");
    this.failureThreshold=failureThreshold;this.openDuration=openDuration;this.clock=clock;
  }

  public boolean allowRequest(String provider){
    return states.computeIfAbsent(provider,ignored->new State()).allow(clock.instant(),openDuration);
  }

  public void record(String provider,ProviderResult.Outcome outcome){
    State state=states.computeIfAbsent(provider,ignored->new State());
    if(outcome==ProviderResult.Outcome.TRANSIENT_ERROR) state.failure(clock.instant(),failureThreshold);
    else state.success();
  }

  private static final class State {
    private int consecutiveFailures;
    private Instant openedAt;
    private boolean probeInFlight;

    synchronized boolean allow(Instant now,Duration openDuration){
      if(openedAt==null) return true;
      if(now.isBefore(openedAt.plus(openDuration)) || probeInFlight) return false;
      probeInFlight=true;
      return true;
    }

    synchronized void failure(Instant now,int threshold){
      probeInFlight=false;
      consecutiveFailures++;
      if(openedAt!=null || consecutiveFailures>=threshold) openedAt=now;
    }

    synchronized void success(){
      consecutiveFailures=0;
      openedAt=null;
      probeInFlight=false;
    }
  }
}
