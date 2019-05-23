package no.nav.bidrag.dokument.consumer;

import java.time.LocalDate;
import java.util.Optional;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import no.nav.bidrag.dokument.dto.AktorDto;
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

  public PersonConsumer(String baseUrl) {
    setDefaultUri(baseUrl);
  }

  public Optional<PersonDto> hentPersonInfo(AktorDto aktorDto) {
    if (aktorDto.erIkkePerson()) {
      return Optional.empty();
    }

    HentPersonRequest request = new HentPersonRequest();
    PersonIdent aktoer = new PersonIdent();
    NorskIdent norskIdent = new NorskIdent();
    norskIdent.setIdent(aktorDto.getIdent());
    aktoer.setIdent(norskIdent);
    request.setAktoer(aktoer);

    //HentPersonResponse response = (HentPersonResponse) getWebServiceTemplate()
    //   .marshalSendAndReceive(request);
    //return mapResponseToDto(response);

    return Optional.ofNullable(byggDummyResponse())
        .map(HentPersonResponse::getPerson)
        .map(this::mapPersonToDto);
  }

  private PersonDto mapPersonToDto(Person person) {
    return new PersonDto(
        person.getPersonnavn() != null ? person.getPersonnavn().getSammensattNavn() : null,
        dodsdato(person),
        person.getDiskresjonskode() != null ? person.getDiskresjonskode().getValue() : null
    );
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
        return null;
      }
    }

    return null;
  }
}