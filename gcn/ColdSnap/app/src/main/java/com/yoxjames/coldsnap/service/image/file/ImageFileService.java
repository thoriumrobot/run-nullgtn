package com.yoxjames.coldsnap.service.image.file;

import android.content.Context;
import com.yoxjames.coldsnap.service.ActionReply;
import java.util.List;
import io.reactivex.Observable;
import javax.annotation.Nullable;

public interface ImageFileService {

    Observable<ActionReply> deleteImageFile(@Nullable() String fileName, @Nullable() Context context);

    Observable<ActionReply> cleanImageFiles(List<String> validFilenames);
}
