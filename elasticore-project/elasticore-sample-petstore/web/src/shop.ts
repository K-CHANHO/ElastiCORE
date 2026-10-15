// Signing in and out - the one part of the API written by hand. Everything
// else the pages call is the generated client: who may call what, and whose
// orders a search returns, is decided by the server from the DSL.
import { request, requestOrNull } from '../../src/main/ts/apiSupport';
import { CustomerDTO } from '../../src/main/ts/CustomerDTO';

const ACCOUNT = '/api/petstore/account';

export const signIn = (login: string, password: string) =>
    request<CustomerDTO>('POST', ACCOUNT + '/signin', { login, password });

export const signUp = (customer: CustomerDTO) =>
    request<CustomerDTO>('POST', ACCOUNT + '/signup', customer);

export const signOut = () => request<void>('POST', ACCOUNT + '/signout');

/** The signed-in customer, or null for a visitor. */
export async function me(): Promise<CustomerDTO | null> {
    try {
        return await requestOrNull<CustomerDTO>('GET', ACCOUNT + '/me');
    } catch {
        return null;
    }
}
