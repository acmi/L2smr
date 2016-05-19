/*
 * Copyright (c) 2016 acmi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package acmi.l2.clientmod.l2smr.model;

import acmi.l2.clientmod.io.UnrealPackage;
import acmi.l2.clientmod.l2smr.StaticMeshActorUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Arrays;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Actor {
    private int ind;
    private String actorName;
    private Offsets offsets;
    private int staticMeshRef;

    private String staticMesh;
    private String actorClass = "Engine.StaticMeshActor";
    private float[] location;
    private int[] rotation;
    private int[] rotationRate;
    private Float scale;
    private float[] scale3D;
    private int[] zoneRenderState;

    public Actor() {
    }

    public Actor(int ind, String actorName, byte[] data, UnrealPackage up) {
        this.ind = ind;
        this.actorName = actorName;
        this.offsets = StaticMeshActorUtil.getOffsets(data, up);
        this.staticMeshRef = StaticMeshActorUtil.getStaticMesh(data, this.offsets);
        if (staticMeshRef != 0) {
            this.staticMesh = up.objectReference(this.staticMeshRef).toString();
            this.location = StaticMeshActorUtil.getLocation(data, this.offsets);
            this.rotation = StaticMeshActorUtil.getRotation(data, this.offsets);
            this.rotationRate = StaticMeshActorUtil.getRotationRate(data, this.offsets);
            this.scale = StaticMeshActorUtil.getDrawScale(data, this.offsets);
            this.scale3D = StaticMeshActorUtil.getDrawScale3D(data, this.offsets);
            this.zoneRenderState = StaticMeshActorUtil.getZoneRenderState(data, this.offsets);
        }
    }

    @JsonIgnore
    public int getInd() {
        return ind;
    }

    @JsonIgnore
    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    @JsonIgnore
    public Offsets getOffsets() {
        return offsets;
    }

    @JsonIgnore
    public int getStaticMeshRef() {
        return staticMeshRef;
    }

    public void setStaticMeshRef(int staticMeshRef) {
        this.staticMeshRef = staticMeshRef;
    }

    public String getStaticMesh() {
        return staticMesh;
    }

    public void setStaticMesh(String staticMesh) {
        this.staticMesh = staticMesh;
    }

    public String getActorClass() {
        return actorClass;
    }

    public void setActorClass(String actorClass) {
        this.actorClass = actorClass;
    }

    @JsonIgnore
    public float[] getLocation() {
        return location;
    }

    public void setLocation(float[] location) {
        this.location = location;
    }

    public Float getX() {
        try {
            return location[0];
        } catch (Exception e) {
            return null;
        }
    }

    public void setX(Float x) {
        if (location == null)
            location = new float[3];
        location[0] = x;
    }

    public Float getY() {
        try {
            return location[1];
        } catch (Exception e) {
            return null;
        }
    }

    public void setY(float y) {
        if (location == null)
            location = new float[3];
        location[1] = y;
    }

    public Float getZ() {
        try {
            return location[2];
        } catch (Exception e) {
            return null;
        }
    }

    public void setZ(float z) {
        if (location == null)
            location = new float[3];
        location[2] = z;
    }

    @JsonIgnore
    public int[] getRotation() {
        return rotation;
    }

    public void setRotation(int[] rotation) {
        this.rotation = rotation;
    }

    public Integer getPitch() {
        try {
            return rotation[0];
        } catch (Exception e) {
            return null;
        }
    }

    public void setPitch(int pitch) {
        if (rotation == null)
            rotation = new int[3];
        rotation[0] = pitch;
    }

    public Integer getYaw() {
        try {
            return rotation[1];
        } catch (Exception e) {
            return null;
        }
    }

    public void setYaw(int yaw) {
        if (rotation == null)
            rotation = new int[3];
        rotation[1] = yaw;
    }

    public Integer getRoll() {
        try {
            return rotation[2];
        } catch (Exception e) {
            return null;
        }
    }

    public void setRoll(int roll) {
        if (rotation == null)
            rotation = new int[3];
        rotation[2] = roll;
    }

    @JsonIgnore
    public int[] getRotationRate() {
        return rotationRate;
    }

    public void setRotationRate(int[] rotationRate) {
        this.rotationRate = rotationRate;
    }

    public Integer getPitchRate() {
        try {
            return rotationRate[0];
        } catch (Exception e) {
            return null;
        }
    }

    public void setPitchRate(int pitch) {
        if (rotationRate == null)
            rotationRate = new int[3];
        rotationRate[0] = pitch;
    }

    public Integer getYawRate() {
        try {
            return rotationRate[1];
        } catch (Exception e) {
            return null;
        }
    }

    public void setYawRate(int yaw) {
        if (rotationRate == null)
            rotationRate = new int[3];
        rotationRate[1] = yaw;
    }

    public Integer getRollRate() {
        try {
            return rotationRate[2];
        } catch (Exception e) {
            return null;
        }
    }

    public void setRollRate(int roll) {
        if (rotationRate == null)
            rotationRate = new int[3];
        rotationRate[2] = roll;
    }

    public Float getScale() {
        return scale;
    }

    public void setScale(Float scale) {
        this.scale = scale;
    }

    @JsonIgnore
    public float[] getScale3D() {
        return scale3D;
    }

    public void setScale3D(float[] scale3D) {
        this.scale3D = scale3D;
    }

    public Float getScaleX() {
        try {
            return scale3D[0];
        } catch (Exception e) {
            return null;
        }
    }

    public void setScaleX(float scaleX) {
        if (scale3D == null)
            scale3D = new float[3];
        scale3D[0] = scaleX;
    }

    public Float getScaleY() {
        try {
            return scale3D[1];
        } catch (Exception e) {
            return null;
        }
    }

    public void setScaleY(float scaleY) {
        if (scale3D == null)
            scale3D = new float[3];
        scale3D[1] = scaleY;
    }

    public Float getScaleZ() {
        try {
            return scale3D[2];
        } catch (Exception e) {
            return null;
        }
    }

    public void setScaleZ(float scaleZ) {
        if (scale3D == null)
            scale3D = new float[3];
        scale3D[2] = scaleZ;
    }

    public int[] getZoneRenderState() {
        return zoneRenderState;
    }

    public void setZoneRenderState(int[] zoneRenderState) {
        this.zoneRenderState = zoneRenderState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        Actor actor = (Actor) o;

        return this.actorName.equals(actor.actorName);
    }

    @Override
    public int hashCode() {
        return this.actorName.hashCode();
    }

    @Override
    public Actor clone() {
        Actor actor = new Actor();
        actor.ind = ind;
        actor.actorName = actorName;
        actor.offsets = offsets.clone();
        actor.staticMeshRef = staticMeshRef;
        actor.staticMesh = staticMesh;
        actor.actorClass = actorClass;
        actor.location = location == null ? null : Arrays.copyOf(location, location.length);
        actor.rotation = rotation == null ? null : Arrays.copyOf(rotation, rotation.length);
        actor.rotationRate = rotationRate == null ? null : Arrays.copyOf(rotationRate, rotationRate.length);
        actor.scale = scale;
        actor.scale3D = scale3D == null ? null : Arrays.copyOf(scale3D, scale3D.length);
        actor.zoneRenderState = zoneRenderState == null ? null : Arrays.copyOf(zoneRenderState, zoneRenderState.length);
        return actor;
    }
}
