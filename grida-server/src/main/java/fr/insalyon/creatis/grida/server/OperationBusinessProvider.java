package fr.insalyon.creatis.grida.server;

import fr.insalyon.creatis.grida.server.business.OperationBusiness;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;

/**
 * Created by abonnet on 5/30/18.
 */
@Component
public abstract class OperationBusinessProvider {

    @Lookup
    public abstract OperationBusiness get(String proxy);
}
