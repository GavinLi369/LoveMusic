package gavin.lovemusic.service;

/**
 * Created by Gavin Li on 2016/1/20.
 *
 */
public enum ActivityCommand{
    PLAY_MUSIC,            //开始播放
    PAUSE_MUSIC,          //暂停播放
    RESUME_MUSIC,          //继续播放
    PREVIOUS_MUSIC,          //上一曲
    NEXT_MUSIC,               //下一曲
    CHANGE_PLAY_MODE,           //改变播放模式
    CHANGE_PROGRESS            //改变播放进度
}
