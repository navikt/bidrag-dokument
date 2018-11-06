const assert = require('assert');
const util = require('util');
const {
    Given,
    When,
    Then
} = require('cucumber');

const {
    kallFasitRestService
} = require('../support/fasit')

function journalpostSuffix(saksnummer) {
    return util.format("/sakjournal/%s", saksnummer)
}

Given('restservice {string}', alias => {
    this.alias = alias;
});

When('jeg henter journalposter for sak {string}', async saksnummer => {
        console.log("henter journalpost", saksnummer, this.alias)
        this.response = await kallFasitRestService(this.alias, journalpostSuffix(saksnummer))
        assert(this.response != null, "Intet svar mottatt fra tjenesten")
        assert(undefined === this.response.errno, "Feilmelding: " + this.response.errno);
});

Then('statuskoden skal være {string}', status => {
    assert.ok(this.response != null, "response er null")
    var r = this.response.response ? this.response.response : this.response;
    // console.log("response objekt", r)
    assert.ok(r.status == status, r.status + " " + r.statusText)
});

Then('skal resultatet være en liste med journalposter', () => {
    assert.ok(Array.isArray(this.response.data), "resultatet er ikke en liste: " + JSON.stringify(this.list));
});

Then('skal resultatet være et journalpost objekt', () => {
    var data = this.response ? this.response.data : null;
    assert.ok(data != null, "posten finnes ikke");
    assert.ok(data.jp_id != null, "journalposten mangler påkrevde properties");
});

Then('hver journalpost i listen skal ha saksnummer {string} i {string} feltet', (saksnummer, prop) => {
    var arr = this.response.data.filter(jp => jp[prop] == saksnummer);
    assert.ok(arr.length == this.response.data.length, "Det finnes forskjellige saksnummer i listen!")
});

Then('objektet skal inneholde følgende verdier', (data) => {
    var expected = JSON.parse(data)
    assert.deepEqual(this.response.data, expected, "Objektene er forskjellige");
})

Then('hver rad i listen skal ha følgende properties satt:', (table) => {
    var missing = [];
    this.response.data.forEach(row => {
        table.rawTable.forEach(item => {
            if (!row[item[0]]) {
                console.log("-- mangler", item[0], "i", row)
                missing.push(item[0])
            }
        })
    })
    assert.ok(missing.length == 0, "Properties mangler: " + missing.join(","))
})
