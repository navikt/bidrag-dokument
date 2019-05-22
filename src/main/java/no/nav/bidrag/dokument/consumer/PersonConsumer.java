package no.nav.bidrag.dokument.consumer;

import java.time.LocalDate;
import java.util.Optional;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import no.nav.bidrag.dokument.dto.PersonDto;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Diskresjonskoder;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Doedsdato;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

public class PersonConsumer extends WebServiceGatewaySupport {

  private String personV3BaseUrl;

  public void setPersonV3BaseUrl(String personV3BaseUrl) {
    this.personV3BaseUrl = personV3BaseUrl;
  }

  public Optional<PersonDto> hentPersonInfo(String id) {
    HentPersonRequest request = new HentPersonRequest();
    PersonIdent aktoer = new PersonIdent();
    NorskIdent norskIdent = new NorskIdent();
    norskIdent.setIdent(id);
    aktoer.setIdent(norskIdent);
    request.setAktoer(aktoer);
    HentPersonResponse response = byggDummyResponse();

    //HentPersonResponse response = (HentPersonResponse) getWebServiceTemplate()
    //   .marshalSendAndReceive(request);
    return mapResponseToDto(response);
  }

  private Optional<PersonDto> mapResponseToDto (HentPersonResponse personResponse) {
    PersonDto dto = new PersonDto();
    Person person = personResponse.getPerson();
    dto.setDiskresjonskode(person.getDiskresjonskode() != null ? person.getDiskresjonskode().getValue() : null);
    dto.setDoedsdato(dodsdato(person));
    dto.setNavn(person.getPersonnavn() != null ? person.getPersonnavn().getSammensattNavn() : null);
    return Optional.of(dto);
  }

  private LocalDate dodsdato(Person person) {
    Doedsdato doedsdato = person.getDoedsdato();
    return doedsdato != null ? toLocalDate(doedsdato.getDoedsdato()) : null;
  }

  private static LocalDate toLocalDate(XMLGregorianCalendar xmlGCal) {
    if (xmlGCal != null) {
      return LocalDate.of(xmlGCal.getYear(), xmlGCal.getMonth(), xmlGCal.getDay());
    }
    return null;
  }

  private HentPersonResponse byggDummyResponse() {
    HentPersonResponse hentPersonResponse = new HentPersonResponse();
    Person person = new Person();

    Diskresjonskoder diskresjonskode = new Diskresjonskoder();
    diskresjonskode.setValue("VABO");
    person.setDiskresjonskode(diskresjonskode);

    Doedsdato doedsdato = new Doedsdato();
    doedsdato.setDoedsdato(toXMLGregorianCalendar(LocalDate.now()));
    person.setDoedsdato(doedsdato);

    Personnavn personnavn = new Personnavn();
    personnavn.setSammensattNavn("Bjarne Bidrag");
    person.setPersonnavn(personnavn);

    hentPersonResponse.setPerson(person);
    return hentPersonResponse;
  }

  private static XMLGregorianCalendar toXMLGregorianCalendar(LocalDate localDate) {
    if (localDate != null) {
      try {
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(localDate.toString());
      } catch (DatatypeConfigurationException dce) {
      }
    }
    return null;
  }
}