package xia.v.lan;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;

import java.util.ArrayList;

/**
 * @author chenhao
 * @description <p>
 * created by chenhao 2019/6/26 16:54
 */
@Data
@Slf4j
public class TestEntity {
    private Integer id;
    private boolean flag;

    public boolean getFlag(){
        return flag;
    }

    public static void main(String[] args) {
        TestEntity t = new TestEntity();
        t.setFlag(Boolean.TRUE);
        

        val t1 = new ArrayList<String>();
        var t2 = new ArrayList<String>();
//        t1 = Lists.newArrayList();
    }
}

