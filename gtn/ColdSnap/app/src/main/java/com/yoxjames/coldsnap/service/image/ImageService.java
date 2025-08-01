/*
 * Copyright (c) 2017 James Yox
 *
 * This file is part of ColdSnap.
 *
 * ColdSnap is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ColdSnap is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ColdSnap.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.yoxjames.coldsnap.service.image;

import com.yoxjames.coldsnap.model.PlantImage;
import com.yoxjames.coldsnap.service.ActionReply;
import java.util.UUID;
import io.reactivex.Observable;
import javax.annotation.Nullable;

/**
 * Created by yoxjames on 10/8/17.
 */
public interface ImageService {

    Observable<PlantImage> getPlantImage(@Nullable() UUID plantUUID);

    Observable<ActionReply> savePlantImage(@Nullable() PlantImage plantImage);

    Observable<ActionReply> deleteImagesForPlant(@Nullable() UUID plantUUID);

    Observable<ActionReply> cleanImagesDirectory();
}
