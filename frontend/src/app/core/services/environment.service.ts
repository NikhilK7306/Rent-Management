import { EnvironmentProviders, makeEnvironmentProviders } from '@angular/core';
import { ENVIRONMENT } from './environment.token';
import { environment } from '../../../environments/environment';

export interface Environment {
  production: boolean;
  apiBaseUrl: string;
}

export function provideEnvironment(): EnvironmentProviders {
  return makeEnvironmentProviders([
    { provide: ENVIRONMENT, useValue: environment }
  ]);
}