package com.example.sysucde.ourapp;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sysucde on 2017-5-28.
 */

public class AuthorInformation {
    String nickname, motto, location;
    int message_id;
    Bitmap header, picture;
    List<Reply> replyList;

    public AuthorInformation(Bitmap header, String nickname, Bitmap picture, String motto, int message_id, String location){
        this.header = header;
        this.nickname = nickname;
        this.picture = picture;
        this.motto = motto;
        this.message_id = message_id;
        this.location = location;
        replyList = new ArrayList<Reply>();
    }

    public void setReply(String writer, String words) {
        replyList.add(new Reply(writer, words));
    }

    public class Reply{
        String writer, words;
        public Reply() {
            writer = "";
            words = "";
        }
        public Reply(String writer, String words) {
            this.writer = writer;
            this.words= words;
        }
    }
}
