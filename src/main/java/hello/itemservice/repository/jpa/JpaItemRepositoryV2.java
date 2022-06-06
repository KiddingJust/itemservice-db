package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
@RequiredArgsConstructor
public class JpaItemRepositoryV2 implements ItemRepository {

    private final SpringDataJpaItemRepository repository;

    @Override
    public Item save(Item item) {
        return repository.save(item);
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = repository.findById(itemId).orElseThrow();
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
    }

    @Override
    public Optional<Item> findById(Long id) {
        //기본 반환 타입이 Optional임
        return repository.findById(id);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        //일단은 직접 동적쿼리 작성.
        //실무에서는 이렇게 하지는 않음. 조건이 아주 적을 때나 고민하는 정도
        if(StringUtils.hasText(itemName) && maxPrice != null){
            return repository.findByItemNameLikeAndPriceLessThanEqual("%"+itemName+"%", maxPrice);
            //return repository.findItems("%" + itemName +"%", maxPrice);
        }else if (StringUtils.hasText(itemName)){
            return repository.findByItemNameLike("%"+itemName+"%");
        }else if (maxPrice != null){
            return repository.findByPriceLessThanEqual(maxPrice);
        }else {
            return repository.findAll();
        }
    }
}
