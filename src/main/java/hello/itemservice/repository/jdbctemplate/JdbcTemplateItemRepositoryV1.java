package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JdbcTEmplate 구현
 */
@Slf4j
public class JdbcTemplateItemRepositoryV1 implements ItemRepository {

    private final JdbcTemplate template;

    //템플릿을 쓰려면 DataSource가 필요
    public JdbcTemplateItemRepositoryV1(DataSource dataSource){
        this.template = new JdbcTemplate(dataSource);
    }

    @Override
    public Item save(Item item) {
        String sql = "insert into item(item_name, price, quantity) values (?, ?, ?)";
        //db에서 만들어준 키값 가져오기
        KeyHolder keyHolder = new GeneratedKeyHolder();
        //영향 받은 row수를 반환함. int num = template.update ~ 로 해줘도 되지.
        template.update(connection -> {
            //자동 증가 키의 경우 이렇게 써야해.. 지금만 이렇게 복잡함.
            //db에 insert를 하고 나서, db에서 만들어준 id값을 가져오는 것.
            //처음에는 비워두고 저장. 그럼 DB에서 대신 생성해줌.
            //-->따라서 애플리케이션은 모름. DB에서 Insert 되고 나서 알 수 있음.
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, item.getItemName());
            ps.setInt(2, item.getPrice());
            ps.setInt(3, item.getQuantity());
            return ps;
        }, keyHolder);

        long key = keyHolder.getKey().longValue();
        item.setId(key);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "update item set item_name=?, price=?, quantity=? where id=?";
        template.update(sql,
                updateParam.getItemName(),
                updateParam.getPrice(),
                updateParam.getQuantity(),
                itemId);
    }

    @Override
    public Optional<Item> findById(Long id) {
        String sql = "select id, item_name, price, quantity where id = ?";
        try {
            //결과를 Item 객체로 변환해야함.
            Item item = template.queryForObject(sql, itemRowMapper(), id);
            return Optional.of(item);
        } catch(EmptyResultDataAccessException e) {
            //queryForObject는 결과가 없으면 예외가 터지므로 무조건 값이 있음.
            //참고로 값이 2개 이상이면 IncorrectResultSizeDataAccessException
            //값이 없으므로 empty 반환
            return Optional.empty();
        }
    }

    private RowMapper<Item> itemRowMapper() {
        return ((rs, rowNum)-> {
            Item item = new Item();
            item.setId(rs.getLong("id"));
            item.setItemName(rs.getString("item_name"));
            item.setPrice(rs.getInt("price"));
            item.setQuantity(rs.getInt("quantity"));
            return item;
        });
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        String sql = "select id, item_name, price, quantity from item";

        /** 동적 쿼리가 복잡함. 일단 복붙 */
        if(StringUtils.hasText(itemName) || maxPrice != null){
            sql += " where";
        }
        boolean andFlag = false;
        List<Object> param = new ArrayList<>();
        if (StringUtils.hasText(itemName)) {
            sql += " item_name like concat('%',?,'%')";
            param.add(itemName);
            andFlag = true;
        }
        if (maxPrice != null) {
            if (andFlag) {
                sql += " and";
            }
            sql += " price <= ?";
            param.add(maxPrice);
        }
        log.info("sql={}", sql);

        //queryForObject는 1개만 가져올 때, query는 List로 가져올 때
        return template.query(sql, itemRowMapper(), param.toArray());
    }
}
