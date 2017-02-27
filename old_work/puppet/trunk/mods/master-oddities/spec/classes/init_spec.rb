require 'spec_helper'
describe 'oddities' do

  context 'with defaults for all parameters' do
    it { should contain_class('oddities') }
  end
end
