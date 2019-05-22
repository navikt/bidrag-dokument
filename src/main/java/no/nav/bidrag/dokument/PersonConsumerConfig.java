package no.nav.bidrag.dokument;

import no.nav.bidrag.dokument.consumer.PersonConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class PersonConsumerConfig {

  @Bean
  public Jaxb2Marshaller marshaller() {
    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    // this package must match the package in the <generatePackage> specified in
    // pom.xml
    marshaller.setContextPath("no.nav.tjeneste.virksomhet.person.v3.meldinger");
    return marshaller;
  }

  @Bean
  public PersonConsumer personConsumer(@Value("${PERSON_V3_URL}") String personV3BaseUrl, Jaxb2Marshaller marshaller) {
    PersonConsumer client = new PersonConsumer();
    client.setDefaultUri(personV3BaseUrl+"/tpsws/ws");
    client.setMarshaller(marshaller);
    client.setUnmarshaller(marshaller);
    client.setPersonV3BaseUrl(personV3BaseUrl);
    return client;
  }
}
