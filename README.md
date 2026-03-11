📖 项目介绍
NovelReaderApp 是针对移动阅读场景开发的安卓小说阅读 APP，整合了在线章节获取、个性化阅读配置、书架管理、用户评论交互等核心功能。APP 采用模块化设计，分离 UI 层与业务逻辑层，通过手势识别、自适应布局优化阅读体验，底层基于 SQLite 实现本地数据持久化，通过 OkHttp 对接远程 API 完成章节内容获取。
核心设计理念
体验优先：支持横竖屏翻页模式，可自定义字体大小、行间距、背景样式
轻量架构：最小化第三方依赖，仅使用 OkHttp 处理网络请求
数据持久化：通过 SharedPreferences 保存阅读设置，SQLite 管理书架与用户数据
网络适配：对接oiapi.net远程 API 服务，实现小说章节在线加载
✨ 功能列表
1. 核心阅读功能
✅ 横竖屏翻页模式切换（默认竖屏滚动）
✅ 字体大小调节（10-30sp，步长 2sp）
✅ 行间距调节（5-30dp，步长 5dp）
✅ 多背景样式切换（内置 10 + 阅读背景）
✅ 竖屏模式下滑至底部自动加载下一章
✅ 手势识别（单击显示 / 隐藏菜单，滑动翻页）
2. 书架管理
✅ 小说加入书架
✅ 书架小说删除
✅ 阅读进度自动记录
✅ 书架列表查询展示
3. 章节管理
✅ 在线章节列表加载
✅ 章节跳转选择（ListView 列表）
✅ 上下章节快速切换
✅ 章节标题展示与进度追踪
4. 用户系统
✅ 用户登录 / 退出
✅ 书籍评论提交与展示
✅ 管理员数据统计（书籍 / 评论数量）
5. 配置管理
✅ 阅读设置持久化（SharedPreferences）
✅ 重启后自动恢复上次阅读模式
🛠️ 开发环境
表格
工具 / 框架	版本要求
Android Studio	2022.3.1 及以上
Gradle	8.13
JDK	17
最小 SDK	24 (Android 7.0)
目标 SDK	34 (Android 14)
网络库	OkHttp 4.x
数据库	SQLite + SharedPreferences
🚀 快速运行步骤
1. 克隆仓库
bash
运行
git clone https://github.com/shiwen409/NovelReaderApp.git
cd NovelReaderApp
2. 环境准备
安装 Android Studio 2022.3.1 或更高版本
在 Project Structure 中配置 JDK 17
确保安装 Android SDK 24-34 版本
同步 Gradle（自动下载依赖包）
3. API 配置
修改app/src/main/java/com/example/novelreaderapp/AppConfig.java中的 API 密钥：
java
运行
public class AppConfig {
    // 替换为从oiapi.net获取的有效密钥
    public static final String API_KEY = "your_api_key_here";
}
API 密钥获取地址：https://www.oiapi.net/
4. 运行应用
连接安卓设备（开启 USB 调试）或启动模拟器
在 Android Studio 中选择 "NovelReaderApp" 项目
点击 "Run" 按钮（绿色三角图标）或使用快捷键Shift+F10
选择目标设备，等待编译安装完成
📁 项目目录结构
plaintext
NovelReaderApp/
├── .gitignore                    # Git忽略文件配置
├── build.gradle                  # 项目级Gradle配置
├── gradle/                       # Gradle包装器文件
├── gradle.properties             # Gradle全局配置
├── settings.gradle               # 模块配置
└── app/                          # 主模块
    ├── build.gradle              # 模块级Gradle配置
    ├── proguard-rules.pro        # 代码混淆规则
    └── src/
        ├── main/
        │   ├── java/com/example/novelreaderapp/
        │   │   ├── activity/     # 所有界面Activity
        │   │   │   ├── ReaderActivity.java    # 核心阅读页面
        │   │   │   ├── LoginActivity.java     # 用户登录页面
        │   │   │   ├── BookshelfActivity.java # 书架管理页面
        │   │   │   ├── BookDetailActivity.java # 书籍详情页
        │   │   │   └── AdminStatisticsActivity.java # 管理员统计页
        │   │   ├── db/           # 数据库管理
        │   │   │   ├── DBHelper.java          # SQLite辅助类
        │   │   │   └── DBManager.java         # 数据库操作封装类
        │   │   ├── model/        # 数据模型
        │   │   │   └── ApiChapterListResponse.java # 章节列表模型
        │   │   ├── utils/        # 工具类
        │   │   │   ├── ApiClient.java         # 网络请求工具
        │   │   │   └── ToastUtils.java        # 提示工具类
        │   │   └── AppConfig.java # 全局配置（API_KEY）
        │   ├── res/              # 资源文件
        │   │   ├── drawable/     # 图片资源（背景、图标）
        │   │   ├── layout/       # 布局文件
        │   │   │   ├── activity_reader.xml    # 阅读页面布局
        │   │   │   ├── activity_login.xml     # 登录页面布局
        │   │   │   └── item_book_shelf.xml    # 书架列表项布局
        │   │   ├── values/       # 字符串/颜色/尺寸资源
        │   │   └── menu/         # 菜单资源
        │   └── AndroidManifest.xml # 应用清单（权限、组件声明）
        └── test/                 # 单元测试目录
❗ 常见问题与解决方案
1. 网络请求失败
现象
提示 "章节加载失败"
API 请求返回错误码
解决
检查AppConfig.java中 API 密钥有效性
确保设备联网（WiFi / 移动数据）
验证oiapi.net服务器状态
确认AndroidManifest.xml已添加网络权限：
xml
<uses-permission android:name="android.permission.INTERNET" />
2. 翻页卡顿
现象
滑动翻页响应缓慢
横屏模式下 UI 卡顿
解决
使用真机测试（模拟器性能受限）
优化calculatePageSize()方法中的页面尺寸计算逻辑
减小drawable/目录下图片资源大小
确保 UI 线程无耗时操作（网络 / 数据库操作移至子线程）
3. 阅读设置不保存
现象
重启 APP 后字体大小 / 背景样式重置
解决
检查 SharedPreferences 保存逻辑：
java
运行
SharedPreferences sp = getSharedPreferences("reader_settings", MODE_PRIVATE);
// 使用apply()而非commit()，避免阻塞UI线程
sp.edit().putFloat("text_size", currentTextSize).apply();
确保在initData()方法中加载设置
Android 10 + 无需申请存储权限即可使用 SharedPreferences
4. Git 推送失败
现象
提示 "Permission denied (publickey)"
提示 "src refspec main does not match any"
解决
SSH 密钥配置：
bash
运行
# 生成SSH密钥
ssh-keygen -t ed25519 -C "你的GitHub邮箱"
# 将公钥添加到GitHub账号
# 测试连接
ssh -T git@github.com
改用 HTTPS 协议推送：
bash
运行
git remote set-url origin https://github.com/shiwen409/NovelReaderApp.git
确保本地分支名为 main：
bash
运行
git branch -M main
5. 章节列表加载失败
现象
阅读页面章节列表为空
提示 "章节列表加载失败"
解决
检查 bookId 参数传递：
java
运行
bookId = getIntent().getStringExtra("bookId");
验证loadChapterList()方法中的章节列表 API 请求逻辑
确保网络请求在子线程执行：
java
运行
new Thread(() -> {
    // 网络请求逻辑
}).start();
📄 开源协议
本项目基于 MIT 协议开源 - 详见 LICENSE 文件。
📧 联系方式
开发者：shiwen409
邮箱：3114426969@qq.com
GitHub：https://github.com/shiwen409
最后更新时间：2026-03-08
最低兼容版本：Android 7.0 (API 24)
目标兼容版本：Android 14 (API 34)
