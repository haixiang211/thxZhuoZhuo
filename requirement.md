- 绘制一个聊天列表
  1. 创建一个 Android 项目
  1. 初始化 git 并提交修改到本地
  1. 将 `RemoteTestLib.aar`导入工程
  1. 提交修改到本地
  1. 调用`DataCenter.init(Context)`初始化
     - 调用此方法后 `Size` 数据会自动赋值
     - 请不要用反射去捞数据
  
  1. 调用`DataCenter.register(OnMessageChangeListener)`注册监听，注册监听后，会不断回调返回 `Message`数据
  
     - ```java
       public class Message {
           public static final int MessageTypeText = 1;
           public static final int MessageTypeImage = 1 << 1;
           private int msgId;  //messageId
           private int msgType; //message 类型  MessageTypeText|MessageTypeImage
           private String content;//message 数据
           private int imageWidth;//MessageTypeImage  图片宽度
           private int imageHeight;//MessageTypeImage  图片高度
       }
       ```
     - `当 msgType == MessageTypeImage` 的时候`DataCenter.loadImage__NotAllowMainThread(content)`可以获取到图片
  
  1. 根据 Message 数据实现一个列表，内容整体靠右，具体参见示例视频

  1. 提交修改到本地
  
  1. 文字和图片都有限制，如下， 需要设置上右下边距, 以下均为Px
     - ```java
       //package: io.zhuozhuo.remotetestlib.Size
       public class Size {
           //文字大小
           public static float message_text_size;
           //文字最大宽度
           public static int message_text_max_width;
           //图片最大宽度
           public static float message_image_max_width;
           //图片最大高度
           public static float message_image_max_height;
           //内容垂直 margin
           public static int message_vertical_margin;
           //内容横向 margin
           public static int message_horizontal_margin;
       }
       ```
    
   1. 示例 ![笔试题效果](笔试题效果.mp4)
   1. 易错点
       1. 新消息
           1. 消息一共22条，必须全部显示
           1. 发送消息线程等待时间超过 200ms 会自动丢掉消息
           1. 不可以阻塞新消息接收显示（例如加载完图片再显示后面的消息
           1. 新 Message 不能引起已经存在的 View、内容 发生变化
           1. 不可以在已存在的 Message 中间插入新的 Message
           1. Message 按照 msgId 升序排列，不可以使用 sort 之类的手动去排序
       1. 消息显示
           1. 文字图片不能超过预设的最大宽高
           1. 仔细看文字提供的 px
           1. 图片需要完全显示，不能变形
           1. 图片超过最大宽高的需等比缩放到 宽或高与预设值相等
           1. 不可以使用 setMaxWidth/setMaxHeight
           1. 最后一张图尺寸 5000x5000, 会触发低版本openGL 限制
       1. 图片不能有内存泄漏，及时处理 bitmap 回收
       1. 不要在主线程加载文件缓存的图片
       1. 注意异步加载图片可能会导致图片显示到错误的地方
       1. 混淆不影响正常使用，请使用 get 获取数据
       1. LruCache 注意缓存大小及元素大小的判断
       1. 复用的 item 不要显示之前的图片
       1. 固定图片显示区域宽高，不要图片显示出来了自动去改变宽高
       1. 图片需要清晰显示
- 进阶（加分项）
  1. 自己实现图片加载、缩放
  1. 自己实现图片内存、文件缓存，文件缓存不要获取存储权限 
  1. 自定义 View，使用 canvas 在同一个 View 里实现图片和文字的绘制
  1. 滑动时停止加载图片，已经加载好的图片正常显示

