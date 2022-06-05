package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@Transactional  //JPA의 모든 데이터 변경은 트랜잭션 안에서 이루어짐.
public class JpaItemRepository implements ItemRepository {

    //필수로 주입
    private final EntityManager em;

    public JpaItemRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Item save(Item item) {
        em.persist(item);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = em.find(Item.class, itemId);
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
        //여기까지만 해주면 끝.
        //트랜잭션 커밋되는 시점에 변경감지 동작으로 업데이트 쿼리가 날아감. 엔티티 변경 감지.
    }

    @Override
    public Optional<Item> findById(Long id) {
        Item item = em.find(Item.class, id);
        return Optional.ofNullable(item);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        //하나를 조회할 땐 식별자 기반으로 쓰지만, 전체 조회는 jpql
        //여기서 Item은 테이블이 아니라 엔티티
        String jpql = "select i from Item i";
        List<Item> result = em.createQuery(jpql, Item.class)
                .getResultList();
        return result;
    }
}
