package karate;

import com.intuit.karate.junit5.Karate;

/** Runner JUnit5 que ejecuta todos los .feature de este paquete. */
class KarateRunner {

    @Karate.Test
    Karate all() {
        return Karate.run().relativeTo(getClass());
    }
}
