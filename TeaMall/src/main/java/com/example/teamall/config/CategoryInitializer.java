package com.example.teamall.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Arrays;
import com.example.teamall.model.Category;
import com.example.teamall.service.CategoryService;
import com.example.teamall.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CategoryInitializer implements CommandLineRunner {

    @Autowired
    private CategoryService categoryService;

    private static final Logger log = LoggerFactory.getLogger(CategoryInitializer.class);

    @Override
    public void run(String... args) {
        initializeCategories();
    }

    private void initializeCategories() {
        try {
            List<Category> categories = Arrays.asList(
                createCategory("白茶", 
                    "白茶属微发酵茶，以芽叶嫩度、白毫多少为优质标准。具有清淡、清香、回甘的特点，代表品种有白毫银针、白牡丹等", 
                    1),
                createCategory("绿茶", 
                    "不发酵茶，保持茶叶原有的绿色，清香扑鼻，滋味鲜爽。代表品种有西湖龙井、碧螺春、黄山毛峰等", 
                    2),
                createCategory("乌龙茶", 
                    "半发酵茶，具有独特的清香与醇厚口感，茶汤橙黄明亮。代表品种有铁观音、大红袍、凤凰单枞等", 
                    3),
                createCategory("红茶", 
                    "全发酵茶，具有红汤、红叶、香甜味浓的特点。代表品种有祁门红茶、正山小种、滇红等", 
                    4),
                createCategory("普洱茶", 
                    "后发酵茶，越陈越香，具有独特的陈香，汤色红浓，口感醇厚。分为生茶和熟茶两大类", 
                    5)
            );

            for (Category category : categories) {
                try {
                    categoryService.create(category);
                    log.info("创建分类: {}", category.getName());
                } catch (BusinessException e) {
                    log.info("分类已存在: {}", category.getName());
                }
            }
            
            log.info("茶叶分类初始化完成");
        } catch (Exception e) {
            log.error("初始化茶叶分类失败", e);
        }
    }

    private Category createCategory(String name, String description, Integer sort) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setSort(sort);
        return category;
    }
} 