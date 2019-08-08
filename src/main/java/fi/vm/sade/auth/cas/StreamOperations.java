package fi.vm.sade.auth.cas;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class StreamOperations {

    private final JdbcOperations jdbcOperations;

    public StreamOperations(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    public <T> Stream<T> stream(String sql, Function<SqlRowSet, T> rowMapper, Object... args) {
        SqlRowSet sqlRowSet = jdbcOperations.queryForRowSet(sql, args);
        RowSetIterator<T> rowSetIterator = new RowSetIterator<>(sqlRowSet, rowMapper);
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(rowSetIterator, Spliterator.IMMUTABLE);
        return StreamSupport.stream(spliterator, false);
    }

    private static class RowSetIterator<T> implements Iterator<T> {

        private final SqlRowSet sqlRowSet;
        private final Function<SqlRowSet, T> rowMapper;
        private T current;

        public RowSetIterator(SqlRowSet sqlRowSet, Function<SqlRowSet, T> rowMapper) {
            this.sqlRowSet = sqlRowSet;
            this.rowMapper = rowMapper;
        }

        @Override
        public boolean hasNext() {
            if (current != null) {
                return true;
            }
            if (sqlRowSet.next()) {
                current = rowMapper.apply(sqlRowSet);
                return true;
            }
            return false;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T value = current;
            current = null;
            return value;
        }

    }

}
