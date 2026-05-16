package com.sky.agent.rag;

import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 知识库加载器
 * 在应用启动时运行，负责将店铺的菜品和套餐数据加载到向量数据库 (EmbeddingStore) 中
 * 以便 RAG (检索增强生成) 系统检索
 */
@Component
@Slf4j
public class KnowledgeLoader implements CommandLineRunner {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private EmbeddingModel embeddingModel;
    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始加载餐厅知识库到向量存储...");

        // 加载所有分类，建立ID到名称的映射，方便后续查询
        List<Category> categories = categoryMapper.list(null);
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        // 加载所有起售中的菜品 (status = 1)
        Dish dishQuery = new Dish();
        dishQuery.setStatus(1);
        List<Dish> dishes = dishMapper.list(dishQuery);

        for (Dish dish : dishes) {
            String categoryName = categoryMap.getOrDefault(dish.getCategoryId(), "未知分类");
            String description = dish.getDescription() == null ? "无描述" : dish.getDescription();

            // 构建菜品的文本描述，将被向量化
            String text = String.format("菜名: %s, 分类: %s, 价格: %s, 描述: %s",
                    dish.getName(), categoryName, dish.getPrice(), description);

            TextSegment segment = TextSegment.from(text, Metadata.from("id", dish.getId().toString()));
            // 生成向量并存入 EmbeddingStore
            embeddingStore.add(embeddingModel.embed(segment).content(), segment);
        }

        // 加载所有起售中的套餐 (status = 1)
        Setmeal setmealQuery = new Setmeal();
        setmealQuery.setStatus(1);
        List<Setmeal> setmeals = setmealMapper.list(setmealQuery);

        for (Setmeal setmeal : setmeals) {
            String categoryName = categoryMap.getOrDefault(setmeal.getCategoryId(), "未知分类");
            String description = setmeal.getDescription() == null ? "无描述" : setmeal.getDescription();

            // 构建套餐的文本描述
             String text = String.format("套餐名: %s, 分类: %s, 价格: %s, 描述: %s",
                    setmeal.getName(), categoryName, setmeal.getPrice(), description);

            TextSegment segment = TextSegment.from(text, Metadata.from("id", setmeal.getId().toString()));
            // 生成向量并存入 EmbeddingStore
            embeddingStore.add(embeddingModel.embed(segment).content(), segment);
        }

        log.info("知识库加载完成。总计条目: {}", dishes.size() + setmeals.size());
    }
}
