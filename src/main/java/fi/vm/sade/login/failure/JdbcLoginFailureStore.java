package fi.vm.sade.login.failure;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class JdbcLoginFailureStore implements LoginFailureStore {

    private final JdbcTemplate jdbcTemplate;

    public JdbcLoginFailureStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public int size(String key) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM login_failure WHERE key = ?", new Object[] {key}, Integer.class);
    }

    @Override
    public boolean remove(String key) {
        return jdbcTemplate.update("DELETE FROM login_failure WHERE key = ?", key) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Long[] get(String key) {
        List<Long> list = jdbcTemplate.query("SELECT time FROM login_failure WHERE key = ? ORDER BY time", new Object[]{key},
                (ResultSet rs, int rowNum) -> rs.getLong("time"));
        return list.toArray(new Long[list.size()]);
    }

    @Override
    public void add(String key, Long time) {
        jdbcTemplate.update("INSERT INTO login_failure (key, time) VALUES (?, ?)", key, time);
    }

    @Override
    public Map<String, Long> clean(int timeLimitInMinutes) {
        long timeLimitInMillis = TimeUnit.MINUTES.toMillis(timeLimitInMinutes);
        long currentTimeInMillis = System.currentTimeMillis();

        return jdbcTemplate.query("DELETE FROM login_failure WHERE key IN (SELECT key FROM login_failure GROUP BY key HAVING MIN(time) + ? < ?) RETURNING *",
                (ResultSet rs, int rowNum) -> rs.getString("key"), timeLimitInMillis, currentTimeInMillis)
                .stream().collect(groupingBy(identity(), counting()));
    }

}
