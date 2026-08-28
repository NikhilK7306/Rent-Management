import { InjectionToken } from '@angular/core';
import { Environment } from './environment.service';

export const ENVIRONMENT = new InjectionToken<Environment>('ENVIRONMENT');