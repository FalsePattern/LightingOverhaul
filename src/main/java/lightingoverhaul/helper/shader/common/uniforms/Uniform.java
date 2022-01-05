package lightingoverhaul.helper.shader.common.uniforms;

public class Uniform {
    protected final int program;
    protected final int location;
    public Uniform(int program, int location) {
        this.program = program;
        this.location = location;
    }

    public int type() {
        return -1;
    }

    public int size() {
        return 1;
    }
}
