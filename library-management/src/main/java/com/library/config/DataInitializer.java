package com.library.config;

import com.library.entity.Book;
import com.library.entity.Category;
import com.library.entity.User;
import com.library.repository.BookRepository;
import com.library.repository.CategoryRepository;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    @Override
    public void run(String... args) {
        // 检查并更新管理员密码
        userRepository.findByUsername("admin").ifPresent(admin -> {
            // 如果密码不匹配，更新为正确的密码
            if (!passwordEncoder.matches("admin123", admin.getPassword())) {
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(User.Role.ADMIN);
                userRepository.save(admin);
                log.info("Admin password has been reset to 'admin123'");
            }
        });

        // 如果没有管理员，创建一个
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .realName("系统管理员")
                    .email("admin@library.com")
                    .role(User.Role.ADMIN)
                    .status(1)
                    .build();
            userRepository.save(admin);
            log.info("Admin user created with password 'admin123'");
        }

        // 初始化分类和书籍数据
        initCategoriesAndBooks();
    }

    private void initCategoriesAndBooks() {
        // 创建分类（如果不存在）
        Category literature = createCategory("文学小说", "包括中外文学名著、现代小说等", null, 1);
        Category science = createCategory("科学技术", "包括计算机、工程、自然科学等", null, 2);
        Category history = createCategory("历史传记", "包括中外历史、人物传记等", null, 3);
        Category philosophy = createCategory("哲学心理", "包括哲学、心理学、社会学等", null, 4);
        Category children = createCategory("儿童读物", "包括童话、绘本、儿童文学等", null, 5);
        Category business = createCategory("经济管理", "包括经济学、管理学、投资理财等", null, 6);

        // 子分类
        Category chineseLit = createCategory("中国文学", "中国古典及现代文学作品", literature, 1);
        Category foreignLit = createCategory("外国文学", "世界各国文学作品", literature, 2);
        Category computer = createCategory("计算机", "编程、软件开发、人工智能等", science, 1);
        Category physics = createCategory("物理学", "物理学相关书籍", science, 2);

        log.info("Categories initialized");

        // 创建书籍 - 中国文学
        createBook("978-7-02-000850-0", "红楼梦", "曹雪芹", "人民文学出版社",
                LocalDate.of(1996, 12, 1), chineseLit, new BigDecimal("59.70"),
                5, "中国古典文学四大名著之一，清代作家曹雪芹创作的长篇小说。", "A-01-001",
                "https://img1.doubanio.com/view/subject/l/public/s1070959.jpg");

        createBook("978-7-02-000851-7", "三国演义", "罗贯中", "人民文学出版社",
                LocalDate.of(1998, 5, 1), chineseLit, new BigDecimal("39.50"),
                4, "中国古典文学四大名著之一，描述东汉末年到西晋初年的历史故事。", "A-01-002",
                "https://img2.doubanio.com/view/subject/l/public/s1076932.jpg");

        createBook("978-7-02-008617-1", "围城", "钱钟书", "人民文学出版社",
                LocalDate.of(1991, 2, 1), chineseLit, new BigDecimal("28.00"),
                3, "中国现代文学经典，讽刺小说的代表作。", "A-01-003",
                "https://img2.doubanio.com/view/subject/l/public/s1070222.jpg");

        createBook("978-7-5442-4261-4", "活着", "余华", "作家出版社",
                LocalDate.of(2012, 8, 1), chineseLit, new BigDecimal("29.00"),
                6, "讲述了农村人福贵悲惨的人生遭遇，展现了一个人对苦难的承受能力。", "A-01-004",
                "https://img2.doubanio.com/view/subject/l/public/s29053580.jpg");

        createBook("978-7-5302-0726-4", "白鹿原", "陈忠实", "人民文学出版社",
                LocalDate.of(1993, 6, 1), chineseLit, new BigDecimal("43.00"),
                3, "描写了陕西关中平原上白鹿村两大家族白家和鹿家的恩怨纷争。", "A-01-005",
                "https://img1.doubanio.com/view/subject/l/public/s28111erta.jpg");

        // 创建书籍 - 外国文学
        createBook("978-7-5447-5763-7", "百年孤独", "加西亚·马尔克斯", "南海出版公司",
                LocalDate.of(2017, 8, 1), foreignLit, new BigDecimal("55.00"),
                4, "魔幻现实主义文学代表作，描写了布恩迪亚家族七代人的传奇故事。", "A-02-001",
                "https://img2.doubanio.com/view/subject/l/public/s27237850.jpg");

        createBook("978-7-5327-5651-7", "1984", "乔治·奥威尔", "上海译文出版社",
                LocalDate.of(2009, 6, 1), foreignLit, new BigDecimal("28.00"),
                5, "反乌托邦政治小说，描写极权主义社会的经典之作。", "A-02-002",
                "https://img1.doubanio.com/view/subject/l/public/s4371408.jpg");

        createBook("978-7-02-010895-8", "傲慢与偏见", "简·奥斯汀", "人民文学出版社",
                LocalDate.of(2014, 9, 1), foreignLit, new BigDecimal("32.00"),
                3, "英国文学史上最受欢迎的小说之一，讲述伊丽莎白与达西的爱情故事。", "A-02-003",
                "https://img2.doubanio.com/view/subject/l/public/s4250062.jpg");

        createBook("978-7-5063-8696-1", "追风筝的人", "卡勒德·胡赛尼", "上海人民出版社",
                LocalDate.of(2006, 5, 1), foreignLit, new BigDecimal("29.00"),
                4, "讲述了阿富汗两个少年之间的友谊和背叛的故事。", "A-02-004",
                "https://img2.doubanio.com/view/subject/l/public/s1727290.jpg");

        createBook("978-7-5447-3638-0", "小王子", "安托万·德·圣埃克苏佩里", "南海出版公司",
                LocalDate.of(2015, 1, 1), foreignLit, new BigDecimal("32.00"),
                8, "世界最畅销的图书之一，以一位飞行员的视角讲述小王子的故事。", "A-02-005",
                "https://img1.doubanio.com/view/subject/l/public/s29238849.jpg");

        // 创建书籍 - 计算机
        createBook("978-7-111-40701-0", "深入理解计算机系统", "Randal E. Bryant", "机械工业出版社",
                LocalDate.of(2016, 11, 1), computer, new BigDecimal("139.00"),
                3, "计算机科学经典教材，从程序员的角度介绍计算机系统。", "B-01-001",
                "https://img9.doubanio.com/view/subject/l/public/s29195878.jpg");

        createBook("978-7-115-42843-4", "算法导论", "Thomas H. Cormen", "机械工业出版社",
                LocalDate.of(2013, 1, 1), computer, new BigDecimal("128.00"),
                2, "计算机算法领域的经典教材，全面介绍算法设计与分析。", "B-01-002",
                "https://img9.doubanio.com/view/subject/l/public/s25648004.jpg");

        createBook("978-7-115-52817-3", "Python编程从入门到实践", "Eric Matthes", "人民邮电出版社",
                LocalDate.of(2020, 10, 1), computer, new BigDecimal("89.00"),
                5, "Python入门经典书籍，适合初学者学习Python编程。", "B-01-003",
                "https://img1.doubanio.com/view/subject/l/public/s29735398.jpg");

        createBook("978-7-111-58408-5", "Java核心技术 卷I", "Cay S. Horstmann", "机械工业出版社",
                LocalDate.of(2019, 12, 1), computer, new BigDecimal("149.00"),
                4, "Java领域最畅销的技术书籍之一，全面介绍Java核心技术。", "B-01-004",
                "https://img2.doubanio.com/view/subject/l/public/s29063065.jpg");

        createBook("978-7-115-52340-6", "JavaScript高级程序设计", "Matt Frisbie", "人民邮电出版社",
                LocalDate.of(2020, 9, 1), computer, new BigDecimal("129.00"),
                3, "JavaScript领域的经典著作，全面介绍JavaScript语言。", "B-01-005",
                "https://img1.doubanio.com/view/subject/l/public/s33703494.jpg");

        // 创建书籍 - 历史传记
        createBook("978-7-101-08612-9", "史记", "司马迁", "中华书局",
                LocalDate.of(2011, 11, 1), history, new BigDecimal("98.00"),
                3, "中国历史上第一部纪传体通史，被鲁迅誉为'史家之绝唱，无韵之离骚'。", "C-01-001",
                "https://img2.doubanio.com/view/subject/l/public/s9986706.jpg");

        createBook("978-7-5086-6609-1", "人类简史", "尤瓦尔·赫拉利", "中信出版社",
                LocalDate.of(2017, 2, 1), history, new BigDecimal("68.00"),
                5, "从十万年前有生命迹象开始到21世纪资本、科技交织的人类发展史。", "C-01-002",
                "https://img3.doubanio.com/view/subject/l/public/s27814883.jpg");

        createBook("978-7-5086-5428-9", "万历十五年", "黄仁宇", "中华书局",
                LocalDate.of(2014, 8, 1), history, new BigDecimal("28.00"),
                4, "以1587年为切入点，展现明代社会的历史面貌。", "C-01-003",
                "https://img9.doubanio.com/view/subject/l/public/s1872653.jpg");

        createBook("978-7-5302-1168-1", "明朝那些事儿", "当年明月", "浙江人民出版社",
                LocalDate.of(2011, 11, 1), history, new BigDecimal("358.20"),
                6, "全景展示明朝十七帝和其他王公权贵的历史故事。", "C-01-004",
                "https://img3.doubanio.com/view/subject/l/public/s2089823.jpg");

        // 创建书籍 - 哲学心理
        createBook("978-7-5086-4497-6", "思考快与慢", "丹尼尔·卡尼曼", "中信出版社",
                LocalDate.of(2012, 7, 1), philosophy, new BigDecimal("69.00"),
                4, "诺贝尔经济学奖得主的经典著作，介绍人类思维的两个系统。", "D-01-001",
                "https://img3.doubanio.com/view/subject/l/public/s6510132.jpg");

        createBook("978-7-5404-7036-8", "乌合之众", "古斯塔夫·勒庞", "中央编译出版社",
                LocalDate.of(2011, 5, 1), philosophy, new BigDecimal("26.00"),
                3, "社会心理学经典著作，研究群体心理特征。", "D-01-002",
                "https://img2.doubanio.com/view/subject/l/public/s5990247.jpg");

        createBook("978-7-5447-7136-7", "被讨厌的勇气", "岸见一郎", "机械工业出版社",
                LocalDate.of(2020, 5, 1), philosophy, new BigDecimal("45.00"),
                5, "阿德勒心理学入门读物，教你如何获得真正的自由和幸福。", "D-01-003",
                "https://img3.doubanio.com/view/subject/l/public/s28382415.jpg");

        // 创建书籍 - 儿童读物
        createBook("978-7-5324-9689-0", "窗边的小豆豆", "黑柳彻子", "南海出版公司",
                LocalDate.of(2018, 3, 1), children, new BigDecimal("36.00"),
                6, "日本最畅销的儿童文学作品，讲述小豆豆在巴学园的成长故事。", "E-01-001",
                "https://img2.doubanio.com/view/subject/l/public/s5979542.jpg");

        createBook("978-7-5411-3619-3", "夏洛的网", "E.B.怀特", "上海译文出版社",
                LocalDate.of(2016, 6, 1), children, new BigDecimal("28.00"),
                5, "一部关于友谊、生命和爱的经典童话故事。", "E-01-002",
                "https://img1.doubanio.com/view/subject/l/public/s2967268.jpg");

        createBook("978-7-5327-6871-8", "绿野仙踪", "莱曼·弗兰克·鲍姆", "上海译文出版社",
                LocalDate.of(2017, 8, 1), children, new BigDecimal("25.00"),
                4, "美国经典童话故事，讲述小女孩多萝西的奇幻冒险。", "E-01-003",
                "https://img9.doubanio.com/view/subject/l/public/s27195825.jpg");

        // 创建书籍 - 经济管理
        createBook("978-7-5086-6552-0", "原则", "瑞·达利欧", "中信出版社",
                LocalDate.of(2018, 1, 1), business, new BigDecimal("98.00"),
                4, "桥水基金创始人的工作和生活原则，畅销全球的管理类书籍。", "F-01-001",
                "https://img1.doubanio.com/view/subject/l/public/s29643861.jpg");

        createBook("978-7-111-38474-8", "创业维艰", "本·霍洛维茨", "中信出版社",
                LocalDate.of(2015, 2, 1), business, new BigDecimal("59.00"),
                3, "硅谷创投教父的经验分享，如何解决创业中的难题。", "F-01-002",
                "https://img3.doubanio.com/view/subject/l/public/s27996320.jpg");

        createBook("978-7-5086-4342-9", "从0到1", "彼得·蒂尔", "中信出版社",
                LocalDate.of(2015, 1, 1), business, new BigDecimal("45.00"),
                5, "PayPal创始人的创业心得，开启商业与未来的秘密。", "F-01-003",
                "https://img9.doubanio.com/view/subject/l/public/s28012945.jpg");

        createBook("978-7-5217-0008-1", "贫穷的本质", "阿比吉特·班纳吉", "中信出版社",
                LocalDate.of(2018, 9, 1), business, new BigDecimal("58.00"),
                3, "诺贝尔经济学奖得主作品，探讨如何逃离贫穷陷阱。", "F-01-004",
                "https://img1.doubanio.com/view/subject/l/public/s33463989.jpg");

        log.info("Initialized {} books", bookRepository.count());
    }

    private Category createCategory(String name, String description, Category parent, int sortOrder) {
        return categoryRepository.findByName(name).orElseGet(() -> {
            Category category = Category.builder()
                    .name(name)
                    .description(description)
                    .parent(parent)
                    .sortOrder(sortOrder)
                    .build();
            return categoryRepository.save(category);
        });
    }

    private void createBook(String isbn, String title, String author, String publisher,
                           LocalDate publishDate, Category category, BigDecimal price,
                           int totalCount, String description, String location, String coverUrl) {
        bookRepository.findByIsbn(isbn).ifPresentOrElse(
            // 如果书籍存在，更新封面
            existingBook -> {
                if (existingBook.getCoverUrl() == null || existingBook.getCoverUrl().isEmpty()) {
                    existingBook.setCoverUrl(coverUrl);
                    bookRepository.save(existingBook);
                    log.debug("Updated cover for book: {}", title);
                }
            },
            // 如果书籍不存在，创建新书籍
            () -> {
                Book book = Book.builder()
                        .isbn(isbn)
                        .title(title)
                        .author(author)
                        .publisher(publisher)
                        .publishDate(publishDate)
                        .category(category)
                        .price(price)
                        .totalCount(totalCount)
                        .availableCount(totalCount)
                        .description(description)
                        .location(location)
                        .coverUrl(coverUrl)
                        .status(1)
                        .build();
                bookRepository.save(book);
            }
        );
    }
}
