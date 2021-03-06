package org.redisson;

import java.util.Collections;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.redisson.api.RScriptReactive;
import org.redisson.client.RedisException;
import org.redisson.core.RScript;

public class RedissonScriptReactiveTest extends BaseReactiveTest {

    @Test
    public void testEval() {
        RScriptReactive script = redisson.getScript();
        List<Object> res = sync(script.<List<Object>>eval(RScript.Mode.READ_ONLY, "return {1,2,3.3333,'\"foo\"',nil,'bar'}", RScript.ReturnType.MULTI, Collections.emptyList()));
        MatcherAssert.assertThat(res, Matchers.<Object>contains(1L, 2L, 3L, "foo"));
    }

    @Test
    public void testScriptExists() {
        RScriptReactive s = redisson.getScript();
        String r = sync(s.scriptLoad("return redis.call('get', 'foo')"));
        Assert.assertEquals("282297a0228f48cd3fc6a55de6316f31422f5d17", r);

        List<Boolean> r1 = sync(s.scriptExists(r));
        Assert.assertEquals(1, r1.size());
        Assert.assertTrue(r1.get(0));

        s.scriptFlush();

        List<Boolean> r2 = sync(s.scriptExists(r));
        Assert.assertEquals(1, r2.size());
        Assert.assertFalse(r2.get(0));
    }

    @Test
    public void testScriptFlush() {
        redisson.getBucket("foo").set("bar");
        String r = sync(redisson.getScript().scriptLoad("return redis.call('get', 'foo')"));
        Assert.assertEquals("282297a0228f48cd3fc6a55de6316f31422f5d17", r);
        String r1 = sync(redisson.getScript().<String>evalSha(RScript.Mode.READ_ONLY, "282297a0228f48cd3fc6a55de6316f31422f5d17", RScript.ReturnType.VALUE, Collections.emptyList()));
        Assert.assertEquals("bar", r1);
        sync(redisson.getScript().scriptFlush());

        try {
            sync(redisson.getScript().evalSha(RScript.Mode.READ_ONLY, "282297a0228f48cd3fc6a55de6316f31422f5d17", RScript.ReturnType.VALUE, Collections.emptyList()));
        } catch (Exception e) {
            Assert.assertEquals(RedisException.class, e.getClass());
        }
    }

    @Test
    public void testScriptLoad() {
        sync(redisson.getBucket("foo").set("bar"));
        String r = sync(redisson.getScript().scriptLoad("return redis.call('get', 'foo')"));
        Assert.assertEquals("282297a0228f48cd3fc6a55de6316f31422f5d17", r);
        String r1 = sync(redisson.getScript().<String>evalSha(RScript.Mode.READ_ONLY, "282297a0228f48cd3fc6a55de6316f31422f5d17", RScript.ReturnType.VALUE, Collections.emptyList()));
        Assert.assertEquals("bar", r1);
    }

    @Test
    public void testEvalSha() {
        RScriptReactive s = redisson.getScript();
        String res = sync(s.scriptLoad("return redis.call('get', 'foo')"));
        Assert.assertEquals("282297a0228f48cd3fc6a55de6316f31422f5d17", res);

        sync(redisson.getBucket("foo").set("bar"));
        String r1 = sync(s.<String>evalSha(RScript.Mode.READ_ONLY, "282297a0228f48cd3fc6a55de6316f31422f5d17", RScript.ReturnType.VALUE, Collections.emptyList()));
        Assert.assertEquals("bar", r1);
    }


}
