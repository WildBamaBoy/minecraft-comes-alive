//package cobalt.minecraft.network.datasync;
//
//import lombok.Getter;
//import net.minecraft.network.datasync.DataParameter;
//
//public class CDataParameter<T> {
//    @Getter private DataParameter<T> mcParameter;
//
//    private CDataParameter(DataParameter<T> param) {
//        this.mcParameter = param;
//    }
//
//    public static <T> CDataParameter fromMC(DataParameter<T> param) {
//        return new CDataParameter<T>(param);
//    }
//}
