package net.ae97.pokebot.extensions.mcping;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BlacklistCheckerTest {
    @BeforeAll
    public static void setupBlacklist() {
        BlacklistChecker.setBlacklist(Lists.newArrayList(
                "play.example.com",
                "*.example.org",
                "*.me",
                "127.0.0.1",
                "192.168.0.*",
                "193.*",
                "10.0.*"
        ));
    }

    @Test
    public void shouldBlacklistExactMatch() {
        assert BlacklistChecker.isHostBlacklisted("play.example.com");
        assert BlacklistChecker.isHostBlacklisted("play.example.com:123456");
        assert BlacklistChecker.isHostBlacklisted("127.0.0.1");
        assert BlacklistChecker.isHostBlacklisted("127.0.0.1:123456");
    }

    @Test
    public void shouldBlacklistPartialMatch() {
        assert BlacklistChecker.isHostBlacklisted("play.example.org");
        assert BlacklistChecker.isHostBlacklisted("play.example.org:123456");
        assert BlacklistChecker.isHostBlacklisted("play.example.me");
        assert BlacklistChecker.isHostBlacklisted("play.example.me:123456");
        assert BlacklistChecker.isHostBlacklisted("192.168.0.1");
        assert BlacklistChecker.isHostBlacklisted("192.168.0.1:123456");
        assert BlacklistChecker.isHostBlacklisted("193.0.0.0");
        assert BlacklistChecker.isHostBlacklisted("193.0.0.0:123456");
        assert BlacklistChecker.isHostBlacklisted("10.0.0.0");
        assert BlacklistChecker.isHostBlacklisted("10.0.0.0:123456");
    }

    @Test
    public void shouldNotCheckInvalidIP() {
        assert !BlacklistChecker.isHostBlacklisted("10.0.0.0.0");
        assert !BlacklistChecker.isHostBlacklisted("10.0.0.-1");
        assert !BlacklistChecker.isHostBlacklisted("10.0.0.257");
    }

    @Test
    public void shouldNotBlacklist() {
        assert !BlacklistChecker.isHostBlacklisted("another.example.com");
        assert !BlacklistChecker.isHostBlacklisted("127.0.0.2");
     }
}