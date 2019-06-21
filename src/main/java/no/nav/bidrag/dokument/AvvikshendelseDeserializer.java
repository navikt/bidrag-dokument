package no.nav.bidrag.dokument;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import no.nav.bidrag.dokument.dto.AvvikType;
import no.nav.bidrag.dokument.dto.Avvikshendelse;
import no.nav.bidrag.dokument.dto.BestillOrginal;

public class AvvikshendelseDeserializer extends StdDeserializer<Avvikshendelse> {

  protected AvvikshendelseDeserializer() {
    super(Avvikshendelse.class);
  }

  @Override
  public Avvikshendelse deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

    var jsonNode = jsonParser.getCodec().readTree(jsonParser);
    var avvikType = AvvikType.valueOf(
        jsonNode.get("avvikType").toString().replaceAll("\"", "")
    );

    if (avvikType == AvvikType.BESTILL_ORGINAL) {
      return new BestillOrginal();
    }

    throw new IllegalStateException("Ukjent avvikshendelse: " + avvikType);
  }
}
