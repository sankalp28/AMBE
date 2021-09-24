package com.tvu.audiomixer.meeting.token;

public interface PackableEx extends Packable {
    void unmarshal(ByteBuf in);
}
