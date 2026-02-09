import { AvailabilityStatus } from '../../services/sponsor.service';

export interface OneChildViewModel {
  id: string;
  name: string;
  ageText: string;
  city: string;
  tags: string[];
  monthlyCost: string;
  trustNote?: string;
  status: AvailabilityStatus;
}
