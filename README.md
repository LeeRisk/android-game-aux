# android-game-aux

## 初衷/目的

- 解放困于游戏中的生产力,提高社会的生产效率;~~咸鱼咸鱼~~
- 按键精灵语法辣鸡,即比不上`scala` 也比不上`python`
- 找图功能辣鸡 不支持各种cnn model 如用vgg16,rec50进行feature提取

### 原理

- 在手机模拟器上运行按键精灵
- 通过按键精灵 截取屏幕保存到文件
- 在将文件发送(通过`curl`)到服务器端进行分析处理(如,找图,找色),决定要执行那些命令(如:点击(X,Y)...)
- 然后在按键精灵上执行这些命令
- 如此往复,周而复始

### 声明

- 此份攻略得分服务器端使用`scala`实现,当然你也可以使用自己熟悉的语言实现;
- 此项目和文档供且仅供学习使用,禁止任何人将其挪威他用
- 此文使用`XXXX`游戏作示例,因为逻辑简单,实现简单


### 目标/效果

### 准备 - android

- curl
copy `curl`(在项目`android/curl`已经准备好了) 到你的手机或模拟器的`/sdcard`(即sd卡)去

### 准备 - 按键精灵(吾曾尝试使用screencap 截取屏幕,然效果不甚理想,故退而使用按键精灵)

- 安装按键精灵android到手机或模拟器上
- 安装按键精灵手机版到电脑上
- 新建一个脚本
- 将[ajjl-script](android/ajjl.script) 复制粘贴进去
- 点击`调试`按钮 应该看到 log : `get result failure or result action is empty`

### 安装python3.6

- 推荐:[andconda](https://www.anaconda.com/download/)
- 安装openvc 执行命令 `pip install opencv-python`
- 安装[jep](https://github.com/ninia/jep) : `pip install jep`
- wind 用户将`<andconda 安装目录>\Lib\site-packages\jep` 添加到环境变量中去
- 如果在不记得andconda的安装目录的话,可以在C盘搜索 `Anaconda3` 说不定能找到)

