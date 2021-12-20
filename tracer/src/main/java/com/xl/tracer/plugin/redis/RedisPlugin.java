package com.xl.tracer.plugin.redis;

import com.xl.tracer.plugin.IPlugin;
import com.xl.tracer.plugin.MatchPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import static net.bytebuddy.matcher.ElementMatchers.namedIgnoreCase;

/**
 * @author tanjl11
 * @date 2021/12/14 16:25
 * redis lettuce
 */
public class RedisPlugin implements IPlugin {
    @Override
    public String name() {
        return "redis";
    }

    @Override
    public MatchPoint[] points() {
        return new MatchPoint[]{
                new MatchPoint() {
                    @Override
                    public ElementMatcher<TypeDescription> buildTypesMatcher() {
                        return ElementMatchers.nameStartsWith("org.springframework.data.redis.connection")
                                .and(ElementMatchers.nameEndsWith("Commands"))
                                ;
                    }

                    @Override
                    public ElementMatcher<MethodDescription> buildMethodsMatcher() {
                        return ElementMatchers.isMethod().
                                and(namedIgnoreCase("zcount").or(namedIgnoreCase("sunionstore"))
                                        .or(namedIgnoreCase("pExpireAt"))
                                        .or(namedIgnoreCase("zunionstore"))
                                        .or(namedIgnoreCase("del")).or(namedIgnoreCase("zinterstore")).or(namedIgnoreCase("echo"))
                                        .or(namedIgnoreCase("hscan")).or(namedIgnoreCase("psubscribe")).or(namedIgnoreCase("type"))
                                        .or(namedIgnoreCase("sinterstore")).or(namedIgnoreCase("setex")).or(namedIgnoreCase("zlexcount"))
                                        .or(namedIgnoreCase("brpoplpush")).or(namedIgnoreCase("bitcount")).or(namedIgnoreCase("llen"))
                                        .or(namedIgnoreCase("zscan")).or(namedIgnoreCase("lpushx")).or(namedIgnoreCase("bitpos"))
                                        .or(namedIgnoreCase("setnx")).or(namedIgnoreCase("hvals")).or(namedIgnoreCase("evalsha"))
                                        .or(namedIgnoreCase("substr")).or(namedIgnoreCase("geodist")).or(namedIgnoreCase("zrangeByLex"))
                                        .or(namedIgnoreCase("geoadd")).or(namedIgnoreCase("expire")).or(namedIgnoreCase("bitop"))
                                        .or(namedIgnoreCase("zrangeByScore")).or(namedIgnoreCase("smove")).or(namedIgnoreCase("lset"))
                                        .or(namedIgnoreCase("decrBy")).or(namedIgnoreCase("pttl")).or(namedIgnoreCase("scan"))
                                        .or(namedIgnoreCase("zrank")).or(namedIgnoreCase("blpop")).or(namedIgnoreCase("rpoplpush"))
                                        .or(namedIgnoreCase("zremrangeByLex")).or(namedIgnoreCase("get")).or(namedIgnoreCase("lpop"))
                                        .or(namedIgnoreCase("persist")).or(namedIgnoreCase("scriptExists")).or(namedIgnoreCase("georadius"))
                                        .or(namedIgnoreCase("set")).or(namedIgnoreCase("srandmember")).or(namedIgnoreCase("incr")).or(namedIgnoreCase("setbit"))
                                        .or(namedIgnoreCase("hexists")).or(namedIgnoreCase("expireAt")).or(namedIgnoreCase("pexpire")).or(namedIgnoreCase("zcard"))
                                        .or(namedIgnoreCase("bitfield")).or(namedIgnoreCase("zrevrangeByLex")).or(namedIgnoreCase("sinter")).or(namedIgnoreCase("srem"))
                                        .or(namedIgnoreCase("getrange")).or(namedIgnoreCase("rename")).or(namedIgnoreCase("zrevrank")).or(namedIgnoreCase("exists"))
                                        .or(namedIgnoreCase("setrange")).or(namedIgnoreCase("zremrangeByRank")).or(namedIgnoreCase("sadd")).or(namedIgnoreCase("sdiff"))
                                        .or(namedIgnoreCase("zrevrange")).or(namedIgnoreCase("getbit")).or(namedIgnoreCase("scard")).or(namedIgnoreCase("sdiffstore"))
                                        .or(namedIgnoreCase("zrevrangeByScore")).or(namedIgnoreCase("zincrby")).or(namedIgnoreCase("rpushx")).or(namedIgnoreCase("psetex"))
                                        .or(namedIgnoreCase("zrevrangeWithScores")).or(namedIgnoreCase("strlen")).or(namedIgnoreCase("hdel")).or(namedIgnoreCase("zremrangeByScore"))
                                        .or(namedIgnoreCase("geohash")).or(namedIgnoreCase("brpop")).or(namedIgnoreCase("lrem")).or(namedIgnoreCase("hlen")).or(namedIgnoreCase("decr"))
                                        .or(namedIgnoreCase("scriptLoad")).or(namedIgnoreCase("lpush")).or(namedIgnoreCase("lindex")).or(namedIgnoreCase("zrange")).or(namedIgnoreCase("incrBy"))
                                        .or(namedIgnoreCase("getSet")).or(namedIgnoreCase("ltrim")).or(namedIgnoreCase("incrByFloat")).or(namedIgnoreCase("rpop")).or(namedIgnoreCase("sort"))
                                        .or(namedIgnoreCase("zrevrangeByScoreWithScores")).or(namedIgnoreCase("pfadd")).or(namedIgnoreCase("eval")).or(namedIgnoreCase("linsert"))
                                        .or(namedIgnoreCase("pfcount")).or(namedIgnoreCase("hkeys")).or(namedIgnoreCase("hsetnx")).or(namedIgnoreCase("hincrBy")).or(namedIgnoreCase("hgetAll"))
                                        .or(namedIgnoreCase("hset")).or(namedIgnoreCase("spop")).or(namedIgnoreCase("zrangeWithScores")).or(namedIgnoreCase("hincrByFloat"))
                                        .or(namedIgnoreCase("hmset")).or(namedIgnoreCase("renamenx")).or(namedIgnoreCase("zrem")).or(namedIgnoreCase("msetnx")).or(namedIgnoreCase("hmget"))
                                        .or(namedIgnoreCase("sunion")).or(namedIgnoreCase("hget")).or(namedIgnoreCase("zadd")).or(namedIgnoreCase("move")).or(namedIgnoreCase("subscribe"))
                                        .or(namedIgnoreCase("geopos")).or(namedIgnoreCase("mset")).or(namedIgnoreCase("zrangeByScoreWithScores")).or(namedIgnoreCase("zscore"))
                                        .or(namedIgnoreCase("pexpireAt")).or(namedIgnoreCase("georadiusByMember")).or(namedIgnoreCase("ttl")).or(namedIgnoreCase("lrange"))
                                        .or(namedIgnoreCase("smembers")).or(namedIgnoreCase("pfmerge")).or(namedIgnoreCase("rpush")).or(namedIgnoreCase("publish"))
                                        .or(namedIgnoreCase("mget")).or(namedIgnoreCase("sscan")).or(namedIgnoreCase("append")).or(namedIgnoreCase("sismember")));

                    }
                }

        };
    }

    @Override
    public Class adviceClass() {
        return RedisAdvice.class;
    }
}
