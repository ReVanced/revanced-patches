package com.amazon.avod.media.ads;

import com.amazon.avod.media.TimeSpan;

public interface AdBreak {
    TimeSpan getDurationExcludingAux();
}