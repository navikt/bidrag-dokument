const assert = require('assert');
const util = require('util');
const {
    Given,
    When,
    Then
} = require('cucumber');

const {
    httpGet, kallFasitRestService
} = require('../support/fasit')

function journalpostSuffix(saksnummer) {
    return util.format("/journalpost/%s", saksnummer)
}

Given('restservice {string}', function (alias) {
    this.alias = alias;
    this.env = process.env.environment;
})

Given('saksnummer {string}', function (saksnummer) {
    this.saksnummer = saksnummer;
})

When('jeg henter journalpost {string}', async (saksnummer) => {
        this.response = await kallFasitRestService(this.alias, journalpostSuffix(saksnummer))
        assert(this.response != null, "Intet svar mottatt fra tjenesten")
        assert(undefined === this.response.errno, "Feilmelding: " + this.response.errno);
});

Then('resultatet skal være en liste med journalposter', function () {
    assert.ok(Array.isArray(this.response.data), "resultatet er ikke en liste: " + JSON.stringify(this.list));
});

Then('skal resultatet være et journalpost objekt', function () {
    var data = this.response ? this.response.data : null;
    assert.ok(data != null, "posten finnes ikke");
    assert.ok(data.jp_id != null, "journalposten mangler påkrevde properties");
});

Then('skal resultatet ikke være et journalpost objekt', function () {
    var data = this.response ? this.response.data : null;
    assert.ok(data == null || data.length == 0, "requested object is not null/empty: " + JSON.stringify(data));
});

Then('statuskoden skal være {string}', function (status) {
    assert.ok(this.response != status, "response er null")
    assert.ok(this.response.status == status, "actual status: " + this.response.status)
});

Then('hver journalpost i listen skal ha saksnummer {string} i {string} feltet', function (saksnummer, prop) {
    var arr = this.response.data.filter(jp => jp[prop] == saksnummer);
    assert.ok(arr.length == this.response.data.length, "Det finnes forskjellige saksnummer i listen!")
});

Then('objektet skal inneholde følgende verdier', function (data) {
    var expected = JSON.parse(data)
    assert.deepEqual(this.response.data, expected, "Objektene er forskjellige");
})

Then('hver rad i listen skal ha følgende properties satt:', function (table) {
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