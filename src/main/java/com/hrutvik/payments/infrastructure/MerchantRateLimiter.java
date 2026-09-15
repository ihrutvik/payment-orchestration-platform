package com.hrutvik.payments.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.List;

@Component
public class MerchantRateLimiter {
  private static final DefaultRedisScript<Long> SCRIPT=new DefaultRedisScript<>(
      "local count=redis.call('INCR',KEYS[1]); "+
      "if count==1 then redis.call('PEXPIRE',KEYS[1],ARGV[2]) end; "+
      "if count>tonumber(ARGV[1]) then return 0 else return 1 end",Long.class);
  private final StringRedisTemplate redis; private final PaymentMetrics metrics;
  private final int limit; private final Duration window; private final boolean failOpen;

  public MerchantRateLimiter(StringRedisTemplate redis,PaymentMetrics metrics,
      @Value("${payments.rate-limit.requests:100}") int limit,
      @Value("${payments.rate-limit.window:1m}") Duration window,
      @Value("${payments.rate-limit.fail-open:true}") boolean failOpen){
    this.redis=redis;this.metrics=metrics;this.limit=limit;this.window=window;this.failOpen=failOpen;
  }

  public boolean allow(String merchantId){
    try{
      Long result=redis.execute(SCRIPT,List.of("payments:rate:"+merchantId),String.valueOf(limit),String.valueOf(window.toMillis()));
      boolean allowed=Long.valueOf(1).equals(result);
      if(!allowed) metrics.rateLimited();
      return allowed;
    }catch(DataAccessException unavailable){
      metrics.rateLimiterUnavailable(failOpen);
      return failOpen;
    }
  }

  public long retryAfterSeconds(){return Math.max(1,window.toSeconds());}
}
