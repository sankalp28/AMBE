package com.tvu.audiomixer.meeting.token;

public interface Packable {
    ByteBuf marshal(ByteBuf out);
}
