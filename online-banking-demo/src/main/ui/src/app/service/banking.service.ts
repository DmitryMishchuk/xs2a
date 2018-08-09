import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {Banking} from '../models/banking.model';
import {SinglePayments} from '../models/models';
import {environment} from '../../environments/environment';


@Injectable({
  providedIn: 'root'
})
export class BankingService {
  mockServerUrl = environment.mockServerUrl;
  savedData = new Banking();

  constructor(private httpClient: HttpClient) {
  }

  postTan(): Observable<any> {
    const body = {
      tanNumber: this.savedData.tan,
      iban: this.savedData.iban,
      consentId: this.savedData.consentId,
      paymentId: this.savedData.paymentId
<<<<<<< HEAD
    }
    var headers = new HttpHeaders({'Content-Type': 'application/json'});
    return this.httpClient.post(this.mockServerUrl + '/view/payment/confirmation/', body, {headers: headers})
=======
    };
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    return this.httpClient.post(this.mockServerUrl + '/payment/confirmation/', body, { headers: headers });
>>>>>>> e752dab5f457b4c2cdf2eba66f032d4d7c7df2a4
  }

  postConsent(decision) {
    const body = {
      iban: this.savedData.iban,
      consentId: this.savedData.consentId,
      paymentId: this.savedData.paymentId
    };

<<<<<<< HEAD
    var headers = new HttpHeaders({'Content-Type': 'application/json'});
    return this.httpClient.post(this.mockServerUrl + '/view/payment/confirmation/consent?decision=' + decision, body, {headers: headers})
=======
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    return this.httpClient.post(this.mockServerUrl + '/payment/confirmation/consent?decision=' + decision, body, { headers: headers });
>>>>>>> e752dab5f457b4c2cdf2eba66f032d4d7c7df2a4
  }

  saveData(data) {
    this.savedData = data;
  }

  loadData() {
    return this.savedData;
  }

  getSinglePayments(): Observable<SinglePayments> {
    return this.httpClient.get<SinglePayments>(this.mockServerUrl + '/payments/' + this.savedData.paymentId).pipe(
      map(data => {
        return data;
      })
    );
  }
}
