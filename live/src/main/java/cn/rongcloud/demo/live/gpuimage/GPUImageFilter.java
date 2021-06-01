/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.demo.live.gpuimage;

import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.FloatBuffer;
import java.util.LinkedList;

public abstract class GPUImageFilter {
    public static final String NO_FILTER_VERTEX_SHADER =
            ""
                    + "attribute vec4 in_pos;\n"
                    + "attribute vec4 in_tc;\n"
                    + " \n"
                    + "varying vec2 interp_tc;\n"
                    + " \n"
                    + "void main()\n"
                    + "{\n"
                    + "    gl_Position = in_pos;\n"
                    + "    interp_tc = in_tc.xy;\n"
                    + "}";
    public static final String NO_FILTER_FRAGMENT_SHADER =
            ""
                    + "varying highp vec2 interp_tc;\n"
                    + " \n"
                    + "uniform sampler2D rgb_tex;\n"
                    + " \n"
                    + "void main()\n"
                    + "{\n"
                    + "     gl_FragColor = texture2D(rgb_tex, interp_tc);\n"
                    + "}";

    private static final FloatBuffer FULL_RECTANGLE_BUF =
            GlUtil.createFloatBuffer(
                    new float[]{
                            -1.0f, -1.0f, // Bottom left.
                            1.0f, -1.0f, // Bottom right.
                            -1.0f, 1.0f, // Top left.
                            1.0f, 1.0f, // Top right.
                    });

    // Texture coordinates - (0, 0) is bottom-left and (1, 1) is top-right.
    private static final FloatBuffer FULL_RECTANGLE_TEX_BUF =
            GlUtil.createFloatBuffer(
                    new float[]{
                            0.0f, 0.0f, // Bottom left.
                            1.0f, 0.0f, // Bottom right.
                            0.0f, 1.0f, // Top left.
                            1.0f, 1.0f // Top right.
                    });

    private final LinkedList<Runnable> mRunOnDraw;
    private final String mVertexShader;
    private final String mFragmentShader;

    protected GlShader mGlShader;
    protected int mOutputWidth;
    protected int mOutputHeight;
    private boolean mIsInitialized;
    private GlRectDrawer glRectDrawer;
    private GlTextureFrameBuffer textureCopy;
    private GlTextureFrameBuffer textureFilter;
    private GPUImageFilter mFilter;

    public GPUImageFilter() {
        this(NO_FILTER_VERTEX_SHADER, NO_FILTER_FRAGMENT_SHADER);
    }

    public GPUImageFilter(final String vertexShader, final String fragmentShader) {
        mRunOnDraw = new LinkedList<Runnable>();
        mVertexShader = vertexShader;
        mFragmentShader = fragmentShader;
        this.mFilter = getFilter();
        glRectDrawer = new GlRectDrawer();
    }

    public static final float[] identityMatrix() {
        return new float[]{
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        };
    }

    // Matrix with transform y' = 1 - y.
    public static final float[] verticalFlipMatrix() {
        return new float[]{
                1, 0, 0, 0,
                0, -1, 0, 0,
                0, 0, 1, 0,
                0, 1, 0, 1
        };
    }

    // Matrix with transform x' = 1 - x.
    public static final float[] horizontalFlipMatrix() {
        return new float[]{
                -1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                1, 0, 0, 1
        };
    }

    public static float[] multiplyMatrices(float[] a, float[] b) {
        final float[] resultMatrix = new float[16];
        Matrix.multiplyMM(resultMatrix, 0, a, 0, b, 0);
        return resultMatrix;
    }

    public final void init() {
        onInit();
        mIsInitialized = true;
        onInitialized();
    }

    public void onInit() {
        mGlShader = new GlShader(mVertexShader, mFragmentShader);
        mIsInitialized = true;
    }

    public void onInitialized() {
    }

    public final void destroy() {
        mIsInitialized = false;
        if (mGlShader != null) mGlShader.release();
        if (textureCopy != null) textureCopy.release();
        if (textureFilter != null) textureFilter.release();
        if (glRectDrawer != null) glRectDrawer.release();
    }

    public void onOutputSizeChanged(final int width, final int height) {
        mOutputWidth = width;
        mOutputHeight = height;
    }

    private void onDraw(final int textureId) {
        mGlShader.useProgram();
        runPendingOnDrawTasks();
        if (!mIsInitialized) {
            return;
        }

        mGlShader.setVertexAttribArray("in_pos", 2, FULL_RECTANGLE_BUF);
        mGlShader.setVertexAttribArray("in_tc", 2, FULL_RECTANGLE_TEX_BUF);
        if (textureId != 0) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glUniform1i(mGlShader.getUniformLocation("rgb_tex"), 0);
        }
        onDrawArraysPre();
        GLES20.glViewport(0, 0, mOutputWidth, mOutputHeight);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    protected void onDrawArraysPre() {
    }

    protected void runPendingOnDrawTasks() {
        while (!mRunOnDraw.isEmpty()) {
            mRunOnDraw.removeFirst().run();
        }
    }

    public boolean isInitialized() {
        return mIsInitialized;
    }

    public int getOutputWidth() {
        return mOutputWidth;
    }

    public int getOutputHeight() {
        return mOutputHeight;
    }

    protected void setInteger(final int location, final int intValue) {
        runOnDraw(
                new Runnable() {
                    @Override
                    public void run() {
                        GLES20.glUniform1i(location, intValue);
                    }
                });
    }

    protected void setFloat(final int location, final float floatValue) {
        runOnDraw(
                new Runnable() {
                    @Override
                    public void run() {
                        GLES20.glUniform1f(location, floatValue);
                    }
                });
    }

    protected void setFloatVec2(final int location, final float[] arrayValue) {
        runOnDraw(
                new Runnable() {
                    @Override
                    public void run() {
                        GLES20.glUniform2fv(location, 1, FloatBuffer.wrap(arrayValue));
                    }
                });
    }

    protected void setFloatVec3(final int location, final float[] arrayValue) {
        runOnDraw(
                new Runnable() {
                    @Override
                    public void run() {
                        GLES20.glUniform3fv(location, 1, FloatBuffer.wrap(arrayValue));
                    }
                });
    }

    protected void setFloatVec4(final int location, final float[] arrayValue) {
        runOnDraw(
                new Runnable() {
                    @Override
                    public void run() {
                        GLES20.glUniform4fv(location, 1, FloatBuffer.wrap(arrayValue));
                    }
                });
    }

    protected void setFloatArray(final int location, final float[] arrayValue) {
        runOnDraw(
                new Runnable() {
                    @Override
                    public void run() {
                        GLES20.glUniform1fv(
                                location, arrayValue.length, FloatBuffer.wrap(arrayValue));
                    }
                });
    }

    protected void setPoint(final int location, final PointF point) {
        runOnDraw(
                new Runnable() {

                    @Override
                    public void run() {
                        float[] vec2 = new float[2];
                        vec2[0] = point.x;
                        vec2[1] = point.y;
                        GLES20.glUniform2fv(location, 1, vec2, 0);
                    }
                });
    }

    protected void setUniformMatrix3f(final int location, final float[] matrix) {
        runOnDraw(
                new Runnable() {

                    @Override
                    public void run() {
                        GLES20.glUniformMatrix3fv(location, 1, false, matrix, 0);
                    }
                });
    }

    protected void setUniformMatrix4f(final int location, final float[] matrix) {
        runOnDraw(
                new Runnable() {

                    @Override
                    public void run() {
                        GLES20.glUniformMatrix4fv(location, 1, false, matrix, 0);
                    }
                });
    }

    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.addLast(runnable);
        }
    }

    public int draw(int width, int height, int rgbTextureId) {

        // if (textureCopy == null) {
        //   textureCopy = new GlTextureFrameBuffer(GLES20.GL_RGBA);
        // }

        // textureCopy.setSize(width, height);

        // GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, textureCopy.getFrameBufferId());
        // GlUtil.checkNoGLES2Error("glBindFramebuffer");

        // Copy the OES texture content. This will also normalize the sampling matrix.
        // glRectDrawer.drawOes(oesTextureId, identityMatrix(), textureCopy.getWidth(),
        //       textureCopy.getHeight(), 0, 0, textureCopy.getWidth(), textureCopy.getHeight());

        if (textureFilter == null) {
            textureFilter = new GlTextureFrameBuffer(GLES20.GL_RGBA);
        }

        textureFilter.setSize(width, height);

        if (!mFilter.isInitialized()) {
            mFilter.init();
        }

        mFilter.onOutputSizeChanged(width, height);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, textureFilter.getFrameBufferId());
        GlUtil.checkNoGLES2Error("glBindFramebuffer");

        mFilter.onDraw(rgbTextureId);
        // Restore normal framebuffer.
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        return textureFilter.getTextureId();
    }

    abstract GPUImageFilter getFilter();
}
